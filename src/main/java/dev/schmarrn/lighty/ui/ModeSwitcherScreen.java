package dev.schmarrn.lighty.ui;

import dev.schmarrn.lighty.SMACH;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.core.lang.I18n;

import java.util.ArrayList;
import java.util.List;

public class ModeSwitcherScreen extends Screen {
	private static final List<Mode> MODES = new ArrayList<>();

	private static final int START_HEIGHT = 15;
	private static final int DELTA_HEIGHT = 24;

	private TooltipElement tooltipElement;

	@Override
	public void init() {
		tooltipElement = new TooltipElement(mc);
		int height = START_HEIGHT + DELTA_HEIGHT;
		I18n i18n = I18n.getInstance();

		buttons.clear();
		buttons.add(new ButtonElement(0, this.width/2 - 75, height, 150, 20, getStatus()));

		height += 6;
		int index = 0;
		for (Mode mode : MODES) {
			height += DELTA_HEIGHT;
			buttons.add(new ButtonElement(index + 2, this.width / 2 - 75, height, 150, 20, i18n.translateKey(mode.title)));
			index++;
		}

		height += DELTA_HEIGHT;
		buttons.add(new ButtonElement(1, this.width/2 - 75, height + 6, 150, 20, i18n.translateKey("gui.lighty.modeSwitcher.done")));
	}

	private String getStatus() {
		I18n i18n = I18n.getInstance();

		return i18n.translateKeyAndFormat(
			"gui.lighty.modeSwitcher.toggle",
			(SMACH.isEnabled()) ? i18n.translateKey("gui.lighty.modeSwitcher.on") : i18n.translateKey("gui.lighty.modeSwitcher.off")
		);
	}

	@Override
	public void render(int mx, int my, float partialTick) {
		I18n i18n = I18n.getInstance();
		renderBackground();
		drawStringCentered(Minecraft.getMinecraft().font, i18n.translateKey("gui.lighty.modeSwitcher.title"), this.width/2, START_HEIGHT, 0xFFFFFF);
		super.render(mx, my, partialTick);

		for (ButtonElement button : buttons) {
			if (button.isHovered(mx, my) && button.id > 1) {
				Mode mode = MODES.get(button.id - 2);
				tooltipElement.render(i18n.translateKey(mode.tooltip), mx, my, 8, -8);
			}
		}
	}

	@Override
	protected void buttonClicked(ButtonElement button) {
		if (button.enabled) {
			if (button.id == 0) {
				SMACH.toggle();
				buttons.get(0).displayString = getStatus();
			} else if (button.id == 1) {
				Minecraft minecraft = Minecraft.getMinecraft();
				minecraft.displayScreen(null);
			} else {
				Mode mode = MODES.get(button.id - 2);
				mode.onPress.run();
				buttons.get(0).displayString = getStatus();
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public static void addButton(String title, String tooltip, Runnable onPress) {
		MODES.add(new Mode(title, tooltip, onPress));
	}

	public static class Mode {
		public String title;
		public String tooltip;
		public Runnable onPress;

		public Mode(String title, String tooltip, Runnable onPress) {
			this.title = title;
			this.tooltip = tooltip;
			this.onPress = onPress;
		}
	}
}

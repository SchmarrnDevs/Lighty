package dev.schmarrn.lighty.ui;

import dev.schmarrn.lighty.ModeLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ModeSwitcherScreen extends Screen {
    private static final List<ButtonWidget> BUTTONS = Lists.newArrayList();
    private static ModeSwitcherScreen current = null;

    private static final int START_HEIGHT = 15;
    private static final int DELTA_HEIGHT = 24;

    public ModeSwitcherScreen() {
        super(Text.translatable("modeSwitcher.lighty.title"));
    }

    @Override
    protected void init() {
        int height = START_HEIGHT + DELTA_HEIGHT;
        this.addDrawableChild(new ButtonWidget(this.width/2 - 75, height, 150, 20, ScreenTexts.OFF, button -> ModeLoader.disable()));
        for (ButtonWidget btn : BUTTONS) {
            height += DELTA_HEIGHT;
            btn.x = this.width / 2 - 75;
            btn.y = height;
            this.addDrawableChild(btn);
        }

        height += DELTA_HEIGHT;
        this.addDrawableChild(new ButtonWidget(this.width/2-75, height + 6, 150, 20, Text.translatable("modeSwitcher.lighty.settings"), button -> MinecraftClient.getInstance().setScreen(new SettingsScreen())));
        height += DELTA_HEIGHT;
        this.addDrawableChild(new ButtonWidget(this.width/2 - 75, height+6, 150, 20, ScreenTexts.DONE, button -> this.close()));
        current = this;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width/2, START_HEIGHT, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static void addButton(Text message, Text tooltip, ButtonWidget.PressAction onPress) {
        BUTTONS.add(new ButtonWidget(0, 0, 150, 20, message, onPress, (button, matrices, mouseX, mouseY) -> {
            if (ModeSwitcherScreen.current == null) return;
            ModeSwitcherScreen.drawCenteredText(matrices, ModeSwitcherScreen.current.textRenderer, tooltip, ModeSwitcherScreen.current.width/2, ModeSwitcherScreen.current.height - ModeSwitcherScreen.current.textRenderer.fontHeight - 5, 0xAAAAAA);
        }));
    }
}

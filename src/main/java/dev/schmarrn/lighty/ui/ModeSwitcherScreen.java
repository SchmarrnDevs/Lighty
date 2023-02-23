package dev.schmarrn.lighty.ui;

import dev.schmarrn.lighty.ModeLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ModeSwitcherScreen extends Screen {
    private static final List<ButtonWidget> BUTTONS = Lists.newArrayList();
    public ModeSwitcherScreen() {
        super(Text.translatable("modeSwitcher.lighty.title"));
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginBottom(4).alignHorizontalCenter().alignVerticalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(1);

        adder.add(ButtonWidget.builder(ScreenTexts.OFF, button -> ModeLoader.disable()).build());

        for (ButtonWidget btn : BUTTONS) {
            adder.add(btn);
        }

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build(), adder.copyPositioner().marginTop(6));
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height/6 - 12, this.width, this.height, 0.5f, 0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width/2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static void addButton(Text message, Text tooltip, ButtonWidget.PressAction onPress) {
        BUTTONS.add(ButtonWidget.builder(message, onPress).tooltip(Tooltip.of(tooltip)).build());
    }
}

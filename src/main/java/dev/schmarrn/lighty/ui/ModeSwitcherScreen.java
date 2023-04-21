package dev.schmarrn.lighty.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.schmarrn.lighty.ModeLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModeSwitcherScreen extends Screen {
    private static final List<Button> BUTTONS = Lists.newArrayList();
    public ModeSwitcherScreen() {
        super(Component.translatable("modeSwitcher.lighty.title"));
    }

    @Override
    protected void init() {
        GridLayout gridWidget = new GridLayout();
        gridWidget.defaultCellSetting().paddingBottom(4).alignHorizontallyCenter().alignVerticallyMiddle();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(1);

        adder.addChild(Button.builder(CommonComponents.OPTION_OFF, button -> ModeLoader.disable()).build());

        for (Button btn : BUTTONS) {
            adder.addChild(btn);
        }

        adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build(), adder.newCellSettings().paddingTop(6));
        gridWidget.arrangeElements();
        FrameLayout.alignInRectangle(gridWidget, 0, this.height/6 - 12, this.width, this.height, 0.5f, 0f);
        gridWidget.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width/2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void addButton(Component message, Component tooltip, Button.OnPress onPress) {
        BUTTONS.add(Button.builder(message, onPress).tooltip(Tooltip.create(tooltip)).build());
    }
}

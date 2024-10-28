// Copyright 2022-2023 The Lighty contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.schmarrn.lighty.ui;

import dev.schmarrn.lighty.Renderers;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class LightyScreen extends Screen {
    private final Screen parent;
    private final boolean isInModMenu;

    public LightyScreen(Screen parent) {
        super(Component.translatable("modeSwitcher.lighty.title"));
        this.parent = parent;
        this.isInModMenu = false;
    }

    public LightyScreen(Screen parent, boolean isInModMenu) {
        super(Component.translatable("modeSwitcher.lighty.title"));
        this.parent = parent;
        this.isInModMenu = isInModMenu;
    }

    @Override
    protected void init() {
        GridLayout gridWidget = new GridLayout();
        gridWidget.defaultCellSetting().paddingBottom(4).alignHorizontallyCenter().alignVerticallyMiddle();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(1);

        if (!isInModMenu) {
            adder.addChild(Button.builder(
                    Component.translatable("lighty.overlay", CommonComponents.optionStatus(SMACH.isEnabled()).getString()),
                    btn -> {
                        SMACH.toggle();
                        btn.setMessage(Component.translatable("lighty.overlay", CommonComponents.optionStatus(SMACH.isEnabled()).getString()));
                    }
            ).tooltip(Tooltip.create(Component.translatable("lighty.overlay.tooltip", KeyBind.toggleKeyBind.getTranslatedKeyMessage().getString()))).build());
        }

        adder.addChild(Button.builder(
                Component.translatable(
                        "lighty.selected",
                        Component.translatable("modeSwitcher." + Renderers.getRenderer().getResourceLocation().toString().replace(":", "."))
                ),
                button -> Minecraft.getInstance().setScreen(new ModeSelectionScreen(this))
        ).tooltip(Tooltip.create(Component.translatable("lighty.selected.tooltip"))).build());
        adder.addChild(Button.builder(
                Component.translatable("modeSwitcher.lighty.settings"),
                button -> Minecraft.getInstance().setScreen(new SettingsScreen(this))
        ).tooltip(Tooltip.create(Component.translatable("modeSwitcher.lighty.settings.tooltip"))).build());

        adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build(), adder.newCellSettings().paddingTop(6));
        gridWidget.arrangeElements();
        FrameLayout.alignInRectangle(gridWidget, 0, this.height/6 - 12, this.width, this.height, 0.5f, 0f);
        gridWidget.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(this.font, this.title, this.width/2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

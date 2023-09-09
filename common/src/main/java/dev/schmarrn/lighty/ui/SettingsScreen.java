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

import com.mojang.serialization.Codec;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SettingsScreen extends Screen {
    public SettingsScreen() {
        super(Component.translatable("settings.lighty.title"));
    }

    OptionsList list;

    @Override
    protected void init() {
        assert this.minecraft != null;
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

        list.addBig(new OptionInstance<>(
                "lighty.options.overlay_distance",
                object -> Tooltip.create(Component.translatable("lighty.options.overlay_distance.tooltip", object)),
                (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                new OptionInstance.IntRange(1, 32),
                Codec.intRange(1, 32),
                Config.getOverlayDistance(),
                Config::setOverlayDistance
        ));

        list.addBig(new OptionInstance<>(
                "lighty.options.overlay_brightness",
                object -> Tooltip.create(Component.translatable("lighty.options.overlay_brightness.tooltip", object)),
                (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                new OptionInstance.IntRange(0, 15),
                Codec.intRange(0, 15),
                Config.getOverlayBrightness(),
                Config::setOverlayBrightness
        ));

        list.addSmall(
                new OptionInstance<>(
                        "lighty.options.block_threshold",
                        object -> Tooltip.create(Component.translatable("lighty.options.block_threshold.tooltip", object)),
                        (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                        new OptionInstance.IntRange(0, 15),
                        Codec.intRange(0, 15),
                        Config.getBlockThreshold(),
                        Config::setBlockThreshold
                ),
                new OptionInstance<>(
                        "lighty.options.sky_threshold",
                        object -> Tooltip.create(Component.translatable("lighty.options.sky_threshold.tooltip", object)),
                        (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                        new OptionInstance.IntRange(0, 15),
                        Codec.intRange(0, 15),
                        Config.getSkyThreshold(),
                        Config::setSkyThreshold
                )

        );

        this.addWidget(list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        list.render(guiGraphics, i, j , f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        Compute.clear();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

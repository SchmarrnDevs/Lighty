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

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SettingsScreen extends OptionsSubScreen {
    private final Screen parent;
    private OptionsList list;
    public SettingsScreen(Screen parent) {
        // passing null to options is fine, because it is only used in the respective OptionsSubScreens,
        // and we don't use it here
        //noinspection DataFlowIssue
        super(parent, null, Component.translatable("settings.lighty.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        list = new OptionsList(this.minecraft, this.width, this.height - 32, this);

        list.addBig(Config.OVERLAY_DISTANCE.getOptionInstance());
        list.addBig(Config.OVERLAY_BRIGHTNESS.getOptionInstance());
        list.addSmall(
                new OptionInstance[]{
                        Config.BLOCK_THRESHOLD.getOptionInstance(),
                        Config.SKY_THRESHOLD.getOptionInstance(),
                }
        );
        list.addBig(Config.FARM_GROWTH_THRESHOLD.getOptionInstance());
        list.addBig(Config.FARM_UPROOT_THRESHOLD.getOptionInstance());
        list.addSmall(
                new OptionInstance[]{
                        Config.SHOW_SAFE.getOptionInstance(),
                        Config.SHOULD_AUTO_ON.getOptionInstance()
                }
        );

        this.addRenderableWidget(list);
        this.layout.addTitleHeader(this.title, this.font);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, $$0 -> this.onClose()).width(200).build());
        this.layout.visitWidgets(this::addRenderableWidget);

        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.list.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        Compute.clear();
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

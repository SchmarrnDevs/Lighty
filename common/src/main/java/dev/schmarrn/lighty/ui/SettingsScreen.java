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
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends OptionsSubScreen {

    public SettingsScreen(Screen parent) {
        // passing null to options is fine, because it is only used in the respective OptionsSubScreens,
        // and we don't use it here
        //noinspection DataFlowIssue
        super(parent, Minecraft.getInstance().options, Component.translatable("settings.lighty.title"));
    }

    @Override
    protected void addOptions() {
        this.list.addBig(Config.OVERLAY_DISTANCE.getOptionInstance());
        this.list.addBig(Config.OVERLAY_BRIGHTNESS.getOptionInstance());
        this.list.addSmall(
                new OptionInstance[]{
                        Config.BLOCK_THRESHOLD.getOptionInstance(),
                        Config.SKY_THRESHOLD.getOptionInstance(),
                }
        );
        this.list.addBig(Config.FARM_GROWTH_THRESHOLD.getOptionInstance());
        this.list.addBig(Config.FARM_UPROOT_THRESHOLD.getOptionInstance());
        this.list.addSmall(
                new OptionInstance[]{
                        Config.SHOW_SAFE.getOptionInstance(),
                        Config.SHOULD_AUTO_ON.getOptionInstance()
                }
        );
    }

    @Override
    public void removed() {
        Compute.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

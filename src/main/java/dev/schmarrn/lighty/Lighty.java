/// Copyright 2022-2023 Andreas Kohler
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

package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyModesRegistration;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.mode.BoringCrossMode;
import dev.schmarrn.lighty.mode.CarpetMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lighty implements ClientModInitializer {
    public static final String MOD_ID = "lighty";
    public static final String MOD_NAME = "Lighty";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Let there be {}", MOD_NAME);

        Config.init();

        KeyBind.init();
        ClientTickEvents.END_CLIENT_TICK.register(Compute::computeCache);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Compute::render);

        CarpetMode.init();
        // NumberMode.init();
        BoringCrossMode.init();

        FabricLoader.getInstance().getEntrypoints("lightyModesRegistration", LightyModesRegistration.class).forEach(LightyModesRegistration::registerLightyModes);

        ModeLoader.setLastUsedMode();
    }
}

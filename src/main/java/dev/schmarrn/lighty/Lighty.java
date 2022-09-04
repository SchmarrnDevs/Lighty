/// Copyright 2022 Andreas Kohler
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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lighty implements ClientModInitializer {
    public static final String MOD_ID = "lighty";
    public static final String MOD_NAME = "Lighty";

    public static Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from {}", MOD_NAME);

        Blocks.init();
        KeyBind.init();

        ClientTickEvents.END_CLIENT_TICK.register(KeyBind::handleKeyBind);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Render::renderOverlay);

        ColorProviderRegistry.BLOCK.register((blockState, blockRenderView, blockPos, i) -> {
            if (blockState.getBlock() instanceof OverlayBlock block) {
                return block.color;
            } else {
                return 0xFFFFFF;
            }
        }, Blocks.GREEN_OVERLAY, Blocks.RED_OVERLAY, Blocks.ORANGE_OVERLAY);
    }
}

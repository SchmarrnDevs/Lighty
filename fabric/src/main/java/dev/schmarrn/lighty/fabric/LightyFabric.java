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

package dev.schmarrn.lighty.fabric;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.core.BufferHolder;
import dev.schmarrn.lighty.core.LightyRenderer;
import dev.schmarrn.lighty.fabric.api.LightyModesRegistration;
import dev.schmarrn.lighty.core.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.overlaystate.SMACH;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;

public class LightyFabric implements ClientModInitializer {
    private static RenderStateDataKey<Object2ObjectOpenHashMap<SectionPos, BufferHolder>> DATA_KEY = RenderStateDataKey.create();
    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(event -> {
            KeyBind.handleKeyBind(Minecraft.getInstance());
            SMACH.updateCompute();
        });

        WorldRenderEvents.END_EXTRACTION.register(t -> {
            var cache = Compute.computeCache(t.camera().blockPosition(), t.world(), t.worldRenderer(), t.frustum());
            if (cache != null && !cache.isEmpty()) {
                t.worldState().setData(DATA_KEY, cache);
            }
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            var cache = context.worldState().getData(DATA_KEY);
            if (cache == null) {
                return;
            }
            var camPos = context.worldState().cameraRenderState.pos;

            LightyRenderer.render(camPos, cache);
        });

        ClientTickEvents.END_CLIENT_TICK.register(KeyBind::handleKeyBind);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((minecraft, level) -> Compute.clear());

        Lighty.init();
        FabricLoader.getInstance().getEntrypoints("lightyModesRegistration", LightyModesRegistration.class).forEach(LightyModesRegistration::registerLightyModes);
        Lighty.postLoad();
    }
}

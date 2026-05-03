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
import dev.schmarrn.lighty.core.*;
import dev.schmarrn.lighty.fabric.api.AddSectionGeometryEvent;
import dev.schmarrn.lighty.fabric.api.LightyModesRegistration;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class LightyFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            KeyBind.handleKeyBind(minecraft);
            SMACH.tick();
            Compute.tick();
        });

        AddSectionGeometryEvent.EVENT.register(((additionalRenderers, sectionOrigin, level) -> {
            if (!Compute.shouldRender()) {
                return;
            }

            for (final var dataProvider : DataProviderRegistry.getActiveProviders()) {
                var dataList = Compute.buildChunk(dataProvider, sectionOrigin, (ClientLevel) level);

                if (dataList == null || dataList.isEmpty()) {
                    return;
                }

                additionalRenderers.add(context -> {
                    Compute.render(
                            dataProvider.getRenderer(),
                            dataList,
                            layer -> context.getOrCreateChunkBuffer((ChunkSectionLayer) layer),
                            (ClientLevel) level,
                            context.getRegion(),
                            context.getBlockRenderer()
                    );
                });
            }
        }));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((minecraft, level) -> Compute.markDirty());

        Lighty.init();
        FabricLoader.getInstance().getEntrypoints("lightyModesRegistration", LightyModesRegistration.class).forEach(LightyModesRegistration::registerLightyModes);
        Lighty.postLoad();
    }
}

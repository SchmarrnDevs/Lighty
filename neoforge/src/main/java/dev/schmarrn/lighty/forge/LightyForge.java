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

package dev.schmarrn.lighty.forge;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.core.BufferHolder;
import dev.schmarrn.lighty.core.LightyRenderer;
import dev.schmarrn.lighty.core.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.overlaystate.SMACH;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Mod(value = Lighty.MOD_ID, dist = Dist.CLIENT)
public class LightyForge {

    private static final Logger log = LoggerFactory.getLogger(LightyForge.class);

    public LightyForge() {
        Lighty.init();
    }


    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are {@linkplain net.neoforged.fml.event.IModBusEvent ModBusEvents}. ModBusEvents are events that are gameload events (fired during game loading or resource reload) and don't have game context.
     */
    @EventBusSubscriber(value = Dist.CLIENT)
    private static class ClassLoadingProtection {

        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            Lighty.postLoad();
        }

        @SubscribeEvent
        public static void Load(LevelEvent.Load event) {
            Compute.clear();
        }
    }

    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are not {@linkplain net.neoforged.fml.event.IModBusEvent ModBusEvents}.
     */
    @EventBusSubscriber(value = Dist.CLIENT, modid = Lighty.MOD_ID)
    private static class ClassLoadingProtection2 {
        @SubscribeEvent
        public static void clientTick(ClientTickEvent.Post event) {
            //Compute.computeCache(Minecraft.getInstance());
            KeyBind.handleKeyBind(Minecraft.getInstance());
            SMACH.updateCompute();
        }

        private static final ContextKey<Object2ObjectOpenHashMap<SectionPos, BufferHolder>> DATA_KEY = new ContextKey<>(
                ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "compute_cache")
        );

        @SubscribeEvent
        public static void extractRenderState(ExtractLevelRenderStateEvent event) {
            var cache = Compute.computeCache(event.getCamera().getBlockPosition(), event.getLevel(), event.getLevelRenderer(), event.getFrustum());
            if (cache != null && !cache.isEmpty()) {
                event.getRenderState().setRenderData(DATA_KEY, cache);
            }
        }

        @SubscribeEvent
        public static void render(RenderLevelStageEvent.AfterTripwireBlocks event) {
            var cache = event.getLevelRenderState().getRenderData(DATA_KEY);
            if (cache == null) {
                return;
            }

            // Prepare render data

            List<RenderPass.Draw<GpuBufferSlice[]>> drawList = new ArrayList<>();
            List<DynamicUniforms.Transform> transforms = new ArrayList<>();

            int biggestBufferSize = 0;
            for (IRenderableSection entry : event.getRenderableSections()) {
                SectionPos sectionPos = SectionPos.of(entry.getRenderOrigin());
                BufferHolder cachedBuffer = cache.get(sectionPos);

                if (cachedBuffer == null) {
                    continue;
                }

                for (var buf : cachedBuffer.getGpuBuffers().entrySet()) {
                    ResourceLocation key = buf.getKey();
                    if (!cachedBuffer.isValid(key)) {
                        continue;
                    }
                    biggestBufferSize = LightyRenderer.addData(sectionPos, buf.getValue(), event.getLevelRenderState().cameraRenderState.pos, biggestBufferSize, drawList, transforms);
                }
            }

            GpuBufferSlice[] dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(transforms.toArray(new DynamicUniforms.Transform[0]));
            LightyRenderer.DrawListData data = new LightyRenderer.DrawListData(drawList, biggestBufferSize, dynamicTransforms);

            // Render

            LightyRenderer.render(data);
        }
    }
}

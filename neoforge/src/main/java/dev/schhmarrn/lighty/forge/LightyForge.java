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

package dev.schhmarrn.lighty.forge;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.event.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(Lighty.MOD_ID)
public class LightyForge {

    public LightyForge() {
        Lighty.init();
    }


    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are {@linkplain net.neoforged.fml.event.IModBusEvent ModBusEvents}. ModBusEvents are events that are gameload events (fired during game loading or resource reload) and don't have game context.
     */
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    private static class ClassLoadingProtection {

        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            Lighty.postLoad();
        }
    }

    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are not {@linkplain net.neoforged.fml.event.IModBusEvent ModBusEvents}.
     */
    @EventBusSubscriber(value = Dist.CLIENT, modid = Lighty.MOD_ID)
    private static class ClassLoadingProtection2 {

        @SubscribeEvent
        public static void clientTick(LevelTickEvent.Post event) {
            Compute.computeCache(Minecraft.getInstance());
            KeyBind.handleKeyBind(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void render(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                Compute.render(event.getFrustum(), event.getPoseStack(), event.getProjectionMatrix());
            }
        }
    }
}

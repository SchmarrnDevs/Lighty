package dev.schhmarrn.lighty.forge;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.event.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Lighty.MOD_ID)
public class LightyForge {

    public LightyForge() {
        Lighty.init();
    }


    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are {@linkplain net.minecraftforge.fml.event.IModBusEvent ModBusEvents}. ModBusEvents are events that are gameload events (fired during game loading or resource reload) and don't have game context.
     */
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    private static class ClassLoadingProtection {

        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            Lighty.postLoad();
        }
    }

    /**
     * This is an inner class to prevent server crashes if this mod is installed on a dedicated server.
     * It's also an EventListener for all events that are not {@linkplain net.minecraftforge.fml.event.IModBusEvent ModBusEvents}.
     */
    @Mod.EventBusSubscriber(Dist.CLIENT)
    private static class ClassLoadingProtection2 {

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Compute.computeCache(Minecraft.getInstance());
                KeyBind.handleKeyBind(Minecraft.getInstance());
            }
        }

        @SubscribeEvent
        public static void render(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                Compute.render(event.getFrustum(), event.getPoseStack(), event.getProjectionMatrix());
            }
        }
    }
}

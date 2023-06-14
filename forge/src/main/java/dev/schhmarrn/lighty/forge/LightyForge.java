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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    private static class ClassLoadingProtection {

        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            Lighty.postLoad();
        }
    }

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

        public static boolean release() {
            Minecraft.getInstance().mouseHandler.releaseMouse();
            return true;
        }
    }
}

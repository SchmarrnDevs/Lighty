package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public class LightUpdateMixin {
    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void lighty$onBlockUpdate(LightLayer lightLayer, SectionPos sectionPos, CallbackInfo ci) {
        Compute.updateSubChunk(sectionPos);
    }
}

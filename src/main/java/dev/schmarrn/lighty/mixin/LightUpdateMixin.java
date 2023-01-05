package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.ModeManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class LightUpdateMixin {
    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void lighty$onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo info) {
//        Lighty.LOGGER.info("BLOCK UPDATE");
        ModeManager.Compute.markDirty();
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
    private void lighty$onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo info) {
//        Lighty.LOGGER.info("CHUNK DELTA UPDATE");
        ModeManager.Compute.markDirty();
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void lighty$onLightUpdate(LightUpdateS2CPacket packet, CallbackInfo info) {
//        Lighty.LOGGER.info("LIGHT UPDATE");
        ModeManager.Compute.markDirty();
    }
}

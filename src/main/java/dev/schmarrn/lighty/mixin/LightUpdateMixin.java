package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class LightUpdateMixin {
    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void lighty$onBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        Compute.updateSubChunkAndSurrounding(SectionPos.of(clientboundBlockUpdatePacket.getPos()));
    }
}

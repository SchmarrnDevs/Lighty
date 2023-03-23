package dev.schmarrn.lighty.mixin;

import com.mojang.brigadier.ParseResults;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class LightUpdateMixin {
    @Shadow private ClientWorld world;

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void lighty$onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo info) {
        Compute.updateSubChunkAndSurrounding(ChunkSectionPos.from(packet. getPos()));
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
    private void lighty$onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo info) {
        if (packet.shouldSkipLightingUpdates()) return;
        //Compute.updateSubChunkAndSurrounding(packet.sectionPos);
        //Compute.markDirty(packet.);
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void lighty$onLightUpdate(LightUpdateS2CPacket packet, CallbackInfo info) {
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX(), packet.getChunkZ()));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() + 1, packet.getChunkZ()));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() - 1, packet.getChunkZ()));
//
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX(), packet.getChunkZ() + 1));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() + 1, packet.getChunkZ() + 1));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() - 1, packet.getChunkZ() + 1));
//
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX(), packet.getChunkZ() - 1));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() + 1, packet.getChunkZ() - 1));
//        Compute.addChunk(world, new ChunkPos(packet.getChunkX() - 1, packet.getChunkZ() - 1));
    }
}

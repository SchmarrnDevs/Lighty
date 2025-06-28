package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface OverlayRenderer {
    void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap);

    ChunkSectionLayer getChunkSectionLayer();
    ResourceLocation getTextureLocation();

    ResourceLocation getResourceLocation();
}

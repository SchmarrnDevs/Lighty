package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;

public interface OverlayRenderer {
    void build(ClientLevel level, OverlayData data, VertexConsumer builder, int lightmap);

    ChunkSectionLayer getOverlaySectionLayer();

    Identifier getTextureLocation();

    Identifier getIdentifier();
}

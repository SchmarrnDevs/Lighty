package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface OverlayRenderer {
    default BufferBuilder beforeBuild(Tesselator builder) {
        return builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    void build(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap);

    void beforeRendering();
    void afterRendering();

    ResourceLocation getResourceLocation();
}

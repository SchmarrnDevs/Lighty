package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface OverlayRenderer {
    void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap);

    RenderPipeline getPipeline();
    VertexFormat getVertexFormat();
    VertexFormat.Mode getVertexFormatMode();
    ResourceLocation getTextureLocation();

    ResourceLocation getResourceLocation();
}

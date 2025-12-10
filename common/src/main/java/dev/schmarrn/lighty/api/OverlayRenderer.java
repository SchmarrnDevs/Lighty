package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public interface OverlayRenderer {
    void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap);

    RenderPipeline getPipeline();
    VertexFormat getVertexFormat();
    VertexFormat.Mode getVertexFormatMode();

    Identifier getTextureLocation();
    default GpuTextureView getTextureView() {
        return Minecraft.getInstance().getTextureManager().getTexture(this.getTextureLocation()).getTextureView();
    }

    Identifier getIdentifier();
}

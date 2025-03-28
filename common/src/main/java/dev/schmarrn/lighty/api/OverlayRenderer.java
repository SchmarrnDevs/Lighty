package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface OverlayRenderer {
    default BufferBuilder beforeBuild(Tesselator builder) {
        return builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    void build(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap);

    default void beforeRendering() {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(getTextureLocation());
        RenderSystem.setShaderTexture(0, tex.getTexture());
    }
    default void afterRendering() {}

    RenderType getRenderType();
    ResourceLocation getTextureLocation();

    ResourceLocation getResourceLocation();
}

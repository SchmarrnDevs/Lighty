package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.core.LightyPipelines;
import dev.schmarrn.lighty.core.LightyVertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class NumberRenderer implements OverlayRenderer {
    private static final float PXL = 1/16f;
    private static final float dx = 0.25f;
    private static final float dz = 0.25f;

    private static void renderDigit(VertexConsumer builder, int digit, float x, float y, float z, int color, int lightmap) {
        float startU = (0b11 & digit) / 4f;
        float startV = ((digit >> 2) & 0b11) / 4f;
        float width = 0.25f;

        builder.addVertex(x, y, z)
                .setColor(color)
                .setUv(startU,startV)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x, y, z + dz)
                .setColor(color)
                .setUv(startU, startV + width)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z + dz)
                .setColor(color)
                .setUv(startU + width, startV + width)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z)
                .setColor(color)
                .setUv(startU + width, startV)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
    }

    private static void renderNumber(VertexConsumer builder, int number, float x, float y, float z, int color, int lightmap) {
        int oneDigit = number % 10;
        int tenDigit = number / 10;

        if (tenDigit > 0) {
            renderDigit(builder, tenDigit, x, y, z, color, lightmap);
            renderDigit(builder, oneDigit, x + dx - PXL, y, z, color, lightmap);
        } else {
            renderDigit(builder, oneDigit, x + (dx - PXL)/2f, y, z, color, lightmap);
        }
    }

    public void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap) {
        float x1 = data.rPos().getX() + PXL * 5.25f;
        float y  = data.rPos().getY() + 1f + 0.005f + data.yOffset();
        float z1 = data.rPos().getZ() + PXL * 4f;

        if (Config.SHOW_SKYLIGHT_LEVEL.getValue()) {
            renderNumber(builder, data.blockNumber(), x1, y, z1, data.color(), lightmap);
            renderNumber(builder, data.skyNumber(), x1, y, z1 + PXL * 6f, data.color(), lightmap);
        } else {
            renderNumber(builder, data.blockNumber(), x1, y, z1 + PXL * 2f, data.color(), lightmap);
        }
    }

    @Override
    public RenderPipeline getPipeline() {
        return LightyPipelines.TERRAIN_CUTOUT;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "textures/block/numbers.png");
    }
    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_number");
    }

    @Override
    public VertexFormat getVertexFormat() {
        return LightyVertexFormat.BLOCK;
    }

    @Override
    public VertexFormat.Mode getVertexFormatMode() {
        return VertexFormat.Mode.QUADS;
    }

    public static void init() {
        var dp = new NumberRenderer();
        ModeManager.registerRenderer(dp.getResourceLocation(), dp);
    }
}

package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class NumberRenderer implements OverlayRenderer {
    private static final float PXL = 1/16f;
    private static final float dx = 0.25f;
    private static final float dz = 0.25f;

    private static void renderDigit(BufferBuilder builder, int digit, float x, float y, float z, int color, int lightmap) {
        float startU = (0b11 & digit) / 4f;
        float startV = ((digit >> 2) & 0b11) / 4f;

        builder.addVertex(x, y, z)
                .setColor(color)
                .setUv(startU,startV)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x, y, z + dz)
                .setColor(color)
                .setUv(startU, startV + 0.25f)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z + dz)
                .setColor(color)
                .setUv(startU + 0.25f, startV + 0.25f)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z)
                .setColor(color)
                .setUv(startU + 0.25f, startV)
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
    }

    private static void renderNumber(BufferBuilder builder, int number, float x, float y, float z, int color, int lightmap) {
        int oneDigit = number % 10;
        int tenDigit = number / 10;

        if (tenDigit > 0) {
            renderDigit(builder, tenDigit, x, y, z, color, lightmap);
            renderDigit(builder, oneDigit, x + dx - PXL, y, z, color, lightmap);
        } else {
            renderDigit(builder, oneDigit, x + (dx - PXL)/2f, y, z, color, lightmap);
        }
    }

    public void build(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap) {
        float x1 = pos.getX() % 16 + PXL * 5.25f;
        float y = pos.getY() % 16 + 1f + 0.005f + data.yOffset();
        float z1 = pos.getZ() % 16 + PXL * 4f;

        if (Config.SHOW_SKYLIGHT_LEVEL.getValue()) {
            renderNumber(builder, data.blockNumber(), x1, y, z1, data.color(), lightmap);
            renderNumber(builder, data.skyNumber(), x1, y, z1 + PXL * 6f, data.color(), lightmap);
        } else {
            renderNumber(builder, data.blockNumber(), x1, y, z1 + PXL * 2f, data.color(), lightmap);
        }
    }

    public void beforeRendering() {
        RenderType.cutout().setupRenderState();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "textures/block/numbers.png"));
    }

    public void afterRendering() {
        RenderType.cutout().clearRenderState();
        RenderSystem.disableDepthTest();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_number");
    }

    public static void init() {
        var dp = new NumberRenderer();
        ModeManager.registerRenderer(dp.getResourceLocation(), dp);
    }
}

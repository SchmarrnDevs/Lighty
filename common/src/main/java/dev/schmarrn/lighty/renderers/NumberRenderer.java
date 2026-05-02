package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class NumberRenderer implements OverlayRenderer {
    private static final float PXL = 1/16f;
    private static final float dx = 0.25f;
    private static final float dz = 0.25f;

    private static void renderDigit(VertexConsumer builder, int digit, float x, float y, float z, int color, int lightmap, TextureAtlasSprite sprite) {
        float startU = (0b11 & digit) / 4f;
        float startV = ((digit >> 2) & 0b11) / 4f;
        float width = 0.25f;

        builder.addVertex(x, y, z)
                .setColor(color)
                .setUv(sprite.getU(startU), sprite.getV(startV))
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x, y, z + dz)
                .setColor(color)
                .setUv(sprite.getU(startU), sprite.getV(startV + width))
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z + dz)
                .setColor(color)
                .setUv(sprite.getU(startU + width), sprite.getV(startV + width))
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(x + dx, y, z)
                .setColor(color)
                .setUv(sprite.getU(startU + width), sprite.getV(startV))
                .setLight(lightmap)
                .setNormal(0f, 1f, 0f);
    }

    private static void renderNumber(VertexConsumer builder, int number, float x, float y, float z, int color, int lightmap, TextureAtlasSprite sprite) {
        int oneDigit = number % 10;
        int tenDigit = number / 10;

        if (tenDigit > 0) {
            renderDigit(builder, tenDigit, x, y, z, color, lightmap, sprite);
            renderDigit(builder, oneDigit, x + dx - PXL, y, z, color, lightmap, sprite);
        } else {
            renderDigit(builder, oneDigit, x + (dx - PXL)/2f, y, z, color, lightmap, sprite);
        }
    }

    public void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap) {
        float x1 = data.rPos().getX() + PXL * 5.25f;
        float y  = data.rPos().getY() + 1f + 0.005f + data.yOffset();
        float z1 = data.rPos().getZ() + PXL * 4f;

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, getTextureLocation()));

        if (Config.SHOW_SKYLIGHT_LEVEL.getValue()) {
            renderNumber(builder, data.blockNumber(), x1, y, z1, data.color(), lightmap, sprite);
            renderNumber(builder, data.skyNumber(), x1, y, z1 + PXL * 6f, data.color(), lightmap, sprite);
        } else {
            renderNumber(builder, data.blockNumber(), x1, y, z1 + PXL * 2f, data.color(), lightmap, sprite);
        }
    }

    @Override
    public ChunkSectionLayer getOverlaySectionLayer() {
        return ChunkSectionLayer.CUTOUT;
    }

    @Override
    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "block/numbers");
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_number");
    }

    public static void init() {
        var dp = new NumberRenderer();
        ModeManager.registerRenderer(dp.getIdentifier(), dp);
    }
}

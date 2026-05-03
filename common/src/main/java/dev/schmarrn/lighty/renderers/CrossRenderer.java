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
import net.minecraft.resources.Identifier;

public class CrossRenderer implements OverlayRenderer {
    public void build(ClientLevel level, OverlayData data, VertexConsumer builder, int lightmap) {
        float x1 = data.rPos().getX();
        float x2 = data.rPos().getX() + 1f;
        float y  = data.rPos().getY() + 1.005f + data.yOffset();
        float z1 = data.rPos().getZ();
        float z2 = data.rPos().getZ() + 1f;

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, getTextureLocation()));

        builder.addVertex(x1, y, z1).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x1, y, z2).setColor(data.color()).setUv(sprite.getU0(), sprite.getV1()).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x2, y, z2).setColor(data.color()).setUv(sprite.getU1(), sprite.getV1()).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x2, y, z1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(0f, 1f, 0f);
    }

    @Override
    public ChunkSectionLayer getOverlaySectionLayer() {
        return ChunkSectionLayer.CUTOUT;
    }

    @Override
    public Identifier getTextureLocation() {
        return Config.CROSS_TEXTURE.getValue();
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_cross");
    }

    public static void init() {
        var dp = new CrossRenderer();
        ModeManager.registerRenderer(dp.getIdentifier(), dp);
    }
}

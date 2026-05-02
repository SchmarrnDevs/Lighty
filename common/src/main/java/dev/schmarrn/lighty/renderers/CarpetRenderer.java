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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CarpetRenderer implements OverlayRenderer {
    public void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap) {
        float x = data.rPos().getX();
        float y = data.rPos().getY() + 1 + data.yOffset();
        float z = data.rPos().getZ();

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, getTextureLocation()));

        try {
            builder.addVertex(x, y + 1 / 16f, z).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x, y + 1 / 16f, z + 1).setColor(data.color()).setUv(sprite.getU0(), sprite.getV1()).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV1()).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(0f, 1f, 0f);
            if (data.yOffset() > 0.001f) {
                //if it renders above it should check if the block above culls the faces
                pos = pos.above();
            }
            //NORTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.SOUTH)), Direction.SOUTH)) {
                builder.addVertex(x, y + 1 / 16f, z + 1).setColor(data.color()).setUv(sprite.getU0(), sprite.getV(1f/16f)).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z + 1).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y, z + 1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV(1f/16f)).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //EAST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.WEST)), Direction.WEST)) {
                builder.addVertex(x, y + 1/16f, z).setColor(data.color()).setUv(sprite.getU0(),sprite.getV(1f/16f)).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z + 1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y + 1/16f, z + 1).setColor(data.color()).setUv(sprite.getU1(), sprite.getV(1f/16f)).setLight(lightmap).setNormal(-1f, 0f, 0f);
            }
            //SOUTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.NORTH)), Direction.NORTH)) {
                builder.addVertex(x+1, y + 1/16f, z).setColor(data.color()).setUv(sprite.getU0(),sprite.getV(1f/16f)).setLight(lightmap).setNormal(0f, 0f, 1f);
                builder.addVertex(x+1, y, z).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y + 1/16f, z).setColor(data.color()).setUv(sprite.getU1(), sprite.getV(1f/16f)).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //WEST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.EAST)), Direction.EAST)) {
                builder.addVertex(x+1, y + 1/16f, z+1).setColor(data.color()).setUv(sprite.getU0(),sprite.getV(1f/16f)).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z+1).setColor(data.color()).setUv(sprite.getU0(), sprite.getV0()).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z).setColor(data.color()).setUv(sprite.getU1(), sprite.getV0()).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y + 1/16f, z).setColor(data.color()).setUv(sprite.getU1(), sprite.getV(1f/16f)).setLight(lightmap).setNormal(1f, 0f, 0f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChunkSectionLayer getOverlaySectionLayer() {
        return ChunkSectionLayer.TRANSLUCENT;
    }

    @Override
    public Identifier getTextureLocation() {
        return Config.CARPET_TEXTURE.getValue();
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_carpet");
    }

    public static void init() {
        var dp = new CarpetRenderer();
        ModeManager.registerRenderer(dp.getIdentifier(), dp);
    }
}

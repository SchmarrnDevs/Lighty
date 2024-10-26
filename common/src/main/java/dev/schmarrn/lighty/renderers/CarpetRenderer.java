package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.api.OverlayData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CarpetRenderer {
    public void compute(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap) {
        float x = pos.getX() % 16;
        float y = pos.getY() % 16 + 1 + data.yOffset();
        float z = pos.getZ() % 16;

        try {
            builder.addVertex(x, y + 1 / 16f, z).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x, y + 1 / 16f, z + 1).setColor(data.color()).setUv(0, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(data.color()).setUv(1, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
            if (data.yOffset() > 0.001f) {
                //if it renders above it should check if the block above culls the faces
                pos = pos.above();
            }
            //NORTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.SOUTH)), Direction.SOUTH)) {
                builder.addVertex(x, y + 1 / 16f, z + 1).setColor(data.color()).setUv(0, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z + 1).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y, z + 1).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(data.color()).setUv(1, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //EAST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.WEST)), Direction.WEST)) {
                builder.addVertex(x, y + 1/16f, z).setColor(data.color()).setUv(0,1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z + 1).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y + 1/16f, z + 1).setColor(data.color()).setUv(1, 1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
            }
            //SOUTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.NORTH)), Direction.NORTH)) {
                builder.addVertex(x+1, y + 1/16f, z).setColor(data.color()).setUv(0,1f/16).setLight(lightmap).setNormal(0f, 0f, 1f);
                builder.addVertex(x+1, y, z).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y + 1/16f, z).setColor(data.color()).setUv(1, 1f/16).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //WEST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), level.getBlockState(pos.relative(Direction.EAST)), Direction.EAST)) {
                builder.addVertex(x+1, y + 1/16f, z+1).setColor(data.color()).setUv(0,1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z+1).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y + 1/16f, z).setColor(data.color()).setUv(1, 1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

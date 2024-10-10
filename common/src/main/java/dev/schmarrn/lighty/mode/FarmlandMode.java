package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extending CarpetMode because the code is largely identical
 */
public class FarmlandMode extends CarpetMode {
    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockPos posUp = pos.above();
        BlockState blockState = world.getBlockState(pos);

        if (!(blockState.getBlock() instanceof FarmBlock)) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);
        int color = LightyColors.getGrowthARGB(blockLightLevel, skyLightLevel);

        float x = pos.getX();
        float y = pos.getY() + 15f/16f;
        float z = pos.getZ();
        int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
        // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
        int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
        //TOP
        builder.addVertex(x, y + 1 / 16f, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x, y + 1 / 16f, z + 1).setColor(color).setUv(0, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(color).setUv(1, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x + 1, y + 1 / 16f, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
        //NORTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.SOUTH)), Direction.SOUTH)) {
            builder.addVertex(x, y + 1 / 16f, z + 1).setColor(color).setUv(0, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
            builder.addVertex(x, y, z + 1).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
            builder.addVertex(x + 1, y, z + 1).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
            builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(color).setUv(1, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
        }
        //EAST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.WEST)), Direction.WEST)) {
            builder.addVertex(x, y + 1/16f, z).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
            builder.addVertex(x, y, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
            builder.addVertex(x, y, z + 1).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
            builder.addVertex(x, y + 1/16f, z + 1).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
        }
        //SOUTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.NORTH)), Direction.NORTH)) {
            builder.addVertex(x+1, y + 1/16f, z).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(0f, 0f, 1f);
            builder.addVertex(x+1, y, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
            builder.addVertex(x, y, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
            builder.addVertex(x, y + 1/16f, z).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(0f, 0f, -1f);
        }
        //WEST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.EAST)), Direction.EAST)) {
            builder.addVertex(x+1, y + 1/16f, z+1).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
            builder.addVertex(x+1, y, z+1).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
            builder.addVertex(x+1, y, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
            builder.addVertex(x+1, y + 1/16f, z).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "farmland_mode");
    }

    public static void init() {
        FarmlandMode mode = new FarmlandMode();
        ModeManager.registerMode(mode.getResourceLocation(), mode);
    }
}
package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyHelper;
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
        BlockState blockStateUp = world.getBlockState(posUp);
        BlockState blockState = world.getBlockState(pos);

        if (!(blockState.getBlock() instanceof FarmBlock)) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);

        if (LightyHelper.isSafe(blockLightLevel) && !Config.getShowSafe()) {
            return;
        }

        int color = LightyColors.getARGB(blockLightLevel, skyLightLevel);

        double offset = LightyHelper.getOffset(blockState, blockStateUp);
        if (offset == -1f) {
            return;
        }

        double x = pos.getX();
        double y = pos.getY() + 1 + offset;
        double z = pos.getZ();
        int overlayBrightness = Config.getOverlayBrightness();
        // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
        int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
        //TOP
        builder.vertex(x, y + 1 / 16f, z).color(color).uv(0, 0).uv2(lightmap).normal(0f, 1f, 0f).endVertex();
        builder.vertex(x, y + 1 / 16f, z + 1).color(color).uv(0, 1).uv2(lightmap).normal(0f, 1f, 0f).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z + 1).color(color).uv(1, 1).uv2(lightmap).normal(0f, 1f, 0f).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z).color(color).uv(1, 0).uv2(lightmap).normal(0f, 1f, 0f).endVertex();
        if (offset > 0.001f) {
            //if it renders above it should check if the block above culls the faces
            pos = pos.above();
        }
        //NORTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.SOUTH, pos.relative(Direction.SOUTH))) {
            builder.vertex(x, y + 1 / 16f, z + 1).color(color).uv(0, 1f / 16).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
            builder.vertex(x, y, z + 1).color(color).uv(0, 0).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
            builder.vertex(x + 1, y, z + 1).color(color).uv(1, 0).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
            builder.vertex(x + 1, y + 1 / 16f, z + 1).color(color).uv(1, 1f / 16).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
        }
        //EAST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.WEST, pos.relative(Direction.WEST))) {
            builder.vertex(x, y + 1/16f, z).color(color).uv(0,1f/16).uv2(lightmap).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(x, y, z).color(color).uv(0, 0).uv2(lightmap).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(x, y, z + 1).color(color).uv(1, 0).uv2(lightmap).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(x, y + 1/16f, z + 1).color(color).uv(1, 1f/16).uv2(lightmap).normal(-1f, 0f, 0f).endVertex();
        }
        //SOUTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.NORTH, pos.relative(Direction.NORTH))) {
            builder.vertex(x+1, y + 1/16f, z).color(color).uv(0,1f/16).uv2(lightmap).normal(0f, 0f, 1f).endVertex();
            builder.vertex(x+1, y, z).color(color).uv(0, 0).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
            builder.vertex(x, y, z).color(color).uv(1, 0).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
            builder.vertex(x, y + 1/16f, z).color(color).uv(1, 1f/16).uv2(lightmap).normal(0f, 0f, -1f).endVertex();
        }
        //WEST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.EAST, pos.relative(Direction.EAST))) {
            builder.vertex(x+1, y + 1/16f, z+1).color(color).uv(0,1f/16).uv2(lightmap).normal(1f, 0f, 0f).endVertex();
            builder.vertex(x+1, y, z+1).color(color).uv(0, 0).uv2(lightmap).normal(1f, 0f, 0f).endVertex();
            builder.vertex(x+1, y, z).color(color).uv(1, 0).uv2(lightmap).normal(1f, 0f, 0f).endVertex();
            builder.vertex(x+1, y + 1/16f, z).color(color).uv(1, 1f/16).uv2(lightmap).normal(1f, 0f, 0f).endVertex();
        }
    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "farmland_mode"), new FarmlandMode());
    }
}
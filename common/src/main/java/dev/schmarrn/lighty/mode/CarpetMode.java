package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.LightyMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class CarpetMode extends LightyMode {

    private static final ModelBlockRenderer renderer = new ModelBlockRenderer(Minecraft.getInstance().getBlockColors());

    public static boolean isBlocked(BlockState block, BlockState up, ClientLevel world, BlockPos upPos) {
        // See SpawnHelper.isClearForSpawn
        // If block with FluidState (think Kelp, Seagrass, Glowlichen underwater), disable overlay
        return (up.isCollisionShapeFullBlock(world, upPos) ||
                up.hasAnalogOutputSignal() ||
                !up.getFluidState().isEmpty()) ||
                up.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) ||
                // MagmaBlocks caused a Crash - But Mobs can still spawn on them, I need to fix this
                block.getBlock() instanceof MagmaBlock;
    }

    @Override
    public void beforeCompute(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockPos posUp = pos.above();
        BlockState up = world.getBlockState(posUp);
        Block upBlock = up.getBlock();
        BlockState block = world.getBlockState(pos);
        boolean validSpawn = upBlock.isPossibleToRespawnInThis(up);
        if (isBlocked(block, up, world, posUp)) {
            return;
        }
        validSpawn = validSpawn && block.isValidSpawn(world, pos, null);

        if (!validSpawn) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);

        int color = LightyColors.getSafeARGB();

        if (blockLightLevel <= Config.getBlockThreshold()) {
            if (skyLightLevel <= Config.getSkyThreshold()) {
                color = LightyColors.getDangerARGB();
            } else {
                color = LightyColors.getWarningARGB();
            }
        }
        double offset = 0;
        if (upBlock instanceof SnowLayerBlock) { // snow layers
            int layer = world.getBlockState(posUp).getValue(SnowLayerBlock.LAYERS);
            // One layer of snow is two pixels high, with one pixel being 1/16
            offset = 2f / 16f * layer;
        } else if (upBlock instanceof CarpetBlock) {
            // Carpet is just one pixel high
            offset = 1f / 16f;
        }
        double x = pos.getX();
        double y = pos.getY() + 1 + offset;
        double z = pos.getZ();
        //TOP
        builder.vertex(x, y + 1 / 16f, z).uv(0, 0).color(color).endVertex();
        builder.vertex(x, y + 1 / 16f, z + 1).uv(0, 1).color(color).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z + 1).uv(1, 1).color(color).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z).uv(1, 0).color(color).endVertex();
        if (offset > 0.001f)
            pos = pos.above();
        //NORTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.SOUTH, pos.relative(Direction.SOUTH))) {
            builder.vertex(x, y + 1 / 16f, z + 1).uv(0, 1f / 16).color(color).endVertex();
            builder.vertex(x, y, z + 1).uv(0, 0).color(color).endVertex();
            builder.vertex(x + 1, y, z + 1).uv(1, 0).color(color).endVertex();
            builder.vertex(x + 1, y + 1 / 16f, z + 1).uv(1, 1f / 16).color(color).endVertex();
        }
        //EAST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.WEST, pos.relative(Direction.WEST))) {
            builder.vertex(x, y + 1/16f, z).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x, y, z).uv(0, 0).color(color).endVertex();
            builder.vertex(x, y, z + 1).uv(1, 0).color(color).endVertex();
            builder.vertex(x, y + 1/16f, z + 1).uv(1, 1f/16).color(color).endVertex();
        }
        //SOUTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.NORTH, pos.relative(Direction.NORTH))) {
            builder.vertex(x+1, y + 1/16f, z).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x+1, y, z).uv(0, 0).color(color).endVertex();
            builder.vertex(x, y, z).uv(1, 0).color(color).endVertex();
            builder.vertex(x, y + 1/16f, z).uv(1, 1f/16).color(color).endVertex();
        }
        //WEST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.EAST, pos.relative(Direction.EAST))) {
            builder.vertex(x+1, y + 1/16f, z+1).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x+1, y, z+1).uv(0, 0).color(color).endVertex();
            builder.vertex(x+1, y, z).uv(1, 0).color(color).endVertex();
            builder.vertex(x+1, y + 1/16f, z).uv(1, 1f/16).color(color).endVertex();
        }
    }

    @Override
    public void beforeRendering() {
        RenderType.translucent().setupRenderState();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(Lighty.MOD_ID, "textures/block/transparent.png"));
    }

    @Override
    public void afterRendering() {
        RenderType.translucent().clearRenderState();
    }
    public static void init() {

        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "carpet_mode"), new CarpetMode());
    }
}

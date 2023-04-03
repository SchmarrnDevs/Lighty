package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.mode.carpet.Blocks;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.mode.carpet.OverlayBlock;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CarpetMode extends LightyMode {
    record Data(BlockState state, double offset) {}

    /**
     * Used in render for the block renderer - DO NOT USE `world.random`, it breaks
     * at least campfires and probably other random rendering things
     */
    private static RandomSource random = RandomSource.create();
    private static PoseStack matrixStack;

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
        matrixStack = new PoseStack();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockPos posUp = pos.above();
        BlockState up = world.getBlockState(posUp);
        Block upBlock = up.getBlock();
        BlockState block = world.getBlockState(pos);
        boolean validSpawn = upBlock.isPossibleToRespawnInThis();
        if (isBlocked(block, up, world, posUp)) {
            return;
        }
        validSpawn = validSpawn && block.isValidSpawn(world, pos, null);

        if (!validSpawn) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);

        BlockState overlayState = Blocks.GREEN_OVERLAY.defaultBlockState();
        if (blockLightLevel == 0) {
            if (skyLightLevel == 0) {
                overlayState = Blocks.RED_OVERLAY.defaultBlockState();
            } else {
                overlayState = Blocks.ORANGE_OVERLAY.defaultBlockState();
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

        matrixStack.pushPose();
        matrixStack.translate(posUp.getX(), (posUp.getY()) + offset, posUp.getZ());
        Minecraft.getInstance().getBlockRenderer().renderBatched(
                overlayState,
                posUp,
                world,
                matrixStack,
                builder,
                false,
                random
        );
        matrixStack.popPose();

        //cache.put(posUp, new Data(overlayState, offset));
    }

    @Override
    public void beforeRendering() {
        RenderSystem.setShader(GameRenderer::getBlockShader);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    @Override
    public void afterRendering() {
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    //    @Override
//    public void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client) {
//        MatrixStack matrixStack = worldRenderContext.matrixStack();
//        Camera camera = worldRenderContext.camera();
//
//        VertexConsumer buffer = provider.getBuffer(RenderLayer.getTranslucent());
//        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
//        matrixStack.push();
//        // Reset matrix position to 0,0,0
//        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
//        cache.forEach((pos, data) -> {
//            double x = pos.getX();
//            double y = pos.getY() + data.offset;
//            double z = pos.getZ();
//
//            boolean overlayVisible = frustum.isVisible(new Box(
//                    x, y, z,
//                    x + 1, y + 1f / 16f, z + 1
//            ));
//
//            if (!overlayVisible) {
//                return;
//            }
//
//            matrixStack.push();
//            matrixStack.translate(x, y, z);
//
//            blockRenderManager.renderBlock(
//                    data.state,
//                    pos,
//                    world,
//                    matrixStack,
//                    buffer,
//                    false,
//                    random
//            );
//
//            matrixStack.pop();
//        });
//
//        matrixStack.pop();
//        provider.draw();
//    }

    public static void init() {
        Blocks.init();

        ColorProviderRegistry.BLOCK.register((blockState, blockRenderView, blockPos, i) -> {
            if (blockState.getBlock() instanceof OverlayBlock block) {
                return block.color;
            } else {
                return 0xFFFFFF;
            }
        }, Blocks.GREEN_OVERLAY, Blocks.RED_OVERLAY, Blocks.ORANGE_OVERLAY);

        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "carpet_mode"), new CarpetMode());
    }
}

package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.LightyMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class NumberMode extends LightyMode {
    record Data(int blockLightLevel, int skyLightLevel, double offset, int color) {}

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

        int color = LightyColors.getSafe();
        if (blockLightLevel == 0) {
            if (skyLightLevel == 0) {
                color = LightyColors.getDanger();
            } else {
                color = LightyColors.getWarning();
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

        //cache.put(posUp, new Data(blockLightLevel, skyLightLevel, offset, color));
    }

//    @Override
//    public void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client) {
//        MatrixStack matrixStack = worldRenderContext.matrixStack();
//        Camera camera = worldRenderContext.camera();
//
//        TextRenderer textRenderer = client.textRenderer;
//        matrixStack.push();
//        // Reset matrix position to 0,0,0
//        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
//        cache.forEach((pos, data) -> {
//            double x = pos.getX() + 0.5;
//            double y = pos.getY() + data.offset + 0.5;
//            double z = pos.getZ() + 0.5;
//
//            boolean overlayVisible = frustum.isVisible(new Box(pos));
//
//            if (!overlayVisible) {
//                return;
//            }
//
//            matrixStack.push();
//            matrixStack.translate(x, y, z);
//            matrixStack.scale(1f/32f, -1f/32f, 1f/32f);
//
//            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) -(Math.PI + camera.getYaw() / 180f * Math.PI)));
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) (camera.getPitch() / 180f * Math.PI)));
//
//            String text = data.skyLightLevel >= 0 ? data.blockLightLevel + "|" + data.skyLightLevel : data.blockLightLevel + "";
//
//            int width = textRenderer.getWidth(text);
//
//            textRenderer.draw(text, -width / 2f, -textRenderer.fontHeight/2f, data.color, true, matrixStack.peek().getPositionMatrix(), provider, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
//
//            matrixStack.pop();
//        });
//
//        matrixStack.pop();
//        provider.draw();
//    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "number_mode"), new NumberMode());
    }
}

package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.LightType;

import java.util.*;

public class Render {
    private static class OverlayData {
        BlockPos pos;
        BlockState state;
        double offset;

        OverlayData(BlockPos pos, BlockState state, double offset) {
            this.pos = pos;
            this.state = state;
            this.offset = offset;
        }
    }

    private static final List<OverlayData> cache = new ArrayList<>();
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;

    private static void computeCache(BlockPos playerPos, ClientWorld world) {
        Lighty.LOGGER.info("NEW COMPUTE");
        for (int x = -16; x <= 16; ++x) {
            for (int y = -16; y <= 16; ++y) {
                for (int z = -16; z <= 16; ++z) {
                    BlockPos pos = new BlockPos(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    BlockPos posUp = pos.up();
                    Block up = world.getBlockState(posUp).getBlock();

                    boolean validSpawn = world.getBlockState(pos).isSolidBlock(world, pos) && up.canMobSpawnInside();

                    if (!validSpawn) {
                        continue;
                    }

                    int blockLightLevel = world.getLightLevel(LightType.BLOCK, posUp);
                    int skyLightLevel = world.getLightLevel(LightType.SKY, posUp);

                    BlockState overlayState = Blocks.GREEN_OVERLAY.getDefaultState();
                    if (blockLightLevel == 0) {
                        if (skyLightLevel == 0) {
                            overlayState = Blocks.RED_OVERLAY.getDefaultState();
                        } else {
                            overlayState = Blocks.ORANGE_OVERLAY.getDefaultState();
                        }
                    }

                    double offset = 0;
                    if (up instanceof SnowBlock) { // snow layers
                        int layer = world.getBlockState(pos).get(SnowBlock.LAYERS);
                        // One layer of snow is two pixels high, with one pixel being 1/16
                        offset = 2f / 16f * layer;
                    } else if (up instanceof CarpetBlock) {
                        // Carpet is just one pixel high
                        offset = 1f / 16f;
                    }

                    cache.add(new OverlayData(posUp, overlayState, offset));
                }
            }
        }
    }

    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static final Random random = new Random();
    private static void renderTranslucent(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum) {
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();

        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        for (OverlayData data : cache) {
            boolean overlayVisible = frustum.isVisible(new Box(
                    data.pos.getX(), data.pos.getY(), data.pos.getZ(),
                    data.pos.getX() + 1, data.pos.getY() + 1f / 16f, data.pos.getZ() + 1
            ));

            if (!overlayVisible) {
                continue;
            }

            matrixStack.push();
            matrixStack.translate(data.pos.getX(), data.pos.getY() + data.offset, data.pos.getZ());

            MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
                    data.state,
                    data.pos,
                    world,
                    matrixStack,
                    provider.getBuffer(RenderLayer.getTranslucent()),
                    false,
                    random
            );

            matrixStack.pop();
        }

        matrixStack.pop();
        provider.draw();
    }

    static void renderOverlay(WorldRenderContext worldRenderContext) {
        if (!KeyBind.enabled) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ClientWorld world = MinecraftClient.getInstance().world;
        Frustum frustum = worldRenderContext.frustum();

        if (player == null || world == null || frustum == null) {
            return;
        }

        BlockPos playerPos = new BlockPos(player.getPos());
        if (!oldPlayerPos.equals(playerPos)) {
            cache.clear();
        }
        oldPlayerPos = playerPos;

        if (cache.isEmpty()) {
            computeCache(playerPos, world);
        }

        // Render
        renderTranslucent(worldRenderContext, world, frustum);
    }

    public static void clearCache() {
        cache.clear();
    }
}

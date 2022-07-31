package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.Random;

public class Render {
    static void renderOverlay(WorldRenderContext worldRenderContext) {
        if (!KeyBind.enabled) return;

        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (player == null || world == null) {
            return;
        }

        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

        VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        for (int x = -16; x <= 16; ++x) {
            for (int y = -16; y <= 16; ++y) {
                for (int z = -16; z <= 16; ++z) {
                    BlockPos pos = new BlockPos(player.getPos().x + x, player.getPos().y + y, player.getPos().z + z);
                    Block up = world.getBlockState(pos.up()).getBlock();

                    if (world.isAir(pos) || !up.canMobSpawnInside() || !world.getBlockState(pos).isSolidBlock(world, pos)) {
                        continue;
                    }

                    int blockLightLevel = world.getLightLevel(LightType.BLOCK, pos.up());
                    int skyLightLevel = world.getLightLevel(LightType.SKY, pos.up());

                    BlockState carpetState = Blocks.GREEN_OVERLAY.getDefaultState();
                    if (blockLightLevel == 0) {
                        if (skyLightLevel == 0) {
                            carpetState = Blocks.RED_OVERLAY.getDefaultState();
                        } else {
                            carpetState = Blocks.ORANGE_OVERLAY.getDefaultState();
                        }
                    }

                    double offset = 0;
                    if (up instanceof SnowBlock) { // snow layers
                        int layer = world.getBlockState(pos.up()).get(SnowBlock.LAYERS);
                        // One layer of snow is one pixel high, with one pixel being 1/16
                        offset = 2f / 16f * layer;
                    } else if (up instanceof CarpetBlock) {
                        // Carpet is just one pixel high
                        offset = 1f / 16f;
                    }

                    matrixStack.push();
                    matrixStack.translate(pos.getX(), pos.getY()+1 + offset, pos.getZ());

                    MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
                            carpetState,
                            pos.up(),
                            world,
                            matrixStack,
                            provider.getBuffer(RenderLayer.getTranslucent()),
                            false,
                            new Random()
                    );

                    matrixStack.pop();
                }
            }
        }
        matrixStack.pop();

        provider.draw();
    }
}

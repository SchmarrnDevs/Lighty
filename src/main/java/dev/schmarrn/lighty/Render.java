package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.LocalRandom;

import java.util.List;
import java.util.Random;

public class Render {
    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static void renderTranslucent(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, List<Compute.OverlayData> cache) {
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();

        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        for (Compute.OverlayData data : cache) {
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
                    new LocalRandom(1337)
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

        // Render
        renderTranslucent(worldRenderContext, world, frustum, Compute.computeCache(playerPos, world));
    }
}

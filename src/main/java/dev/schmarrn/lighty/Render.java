package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.LocalRandom;

import java.util.List;
import java.util.Random;

public class Render {
    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    //private static final BlockRenderManager blockRenderManager = client.getBlockRenderManager();
    //private static final VertexConsumer buffer = provider.getBuffer(RenderLayer.getTranslucent());

    private static void renderTranslucent(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, List<Compute.OverlayData> cache) {
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();

        VertexConsumer buffer = provider.getBuffer(RenderLayer.getTranslucent());
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        for (Compute.OverlayData data : cache) {
            double x = data.pos.getX(),
                    y = data.pos.getY() + data.offset,
                    z = data.pos.getZ();
            boolean overlayVisible = frustum.isVisible(new Box(
                    x, y, z,
                    x + 1, y + 1f / 16f, z + 1
            ));

            if (!overlayVisible) {
                continue;
            }

            matrixStack.push();
            matrixStack.translate(x, y, z);

            blockRenderManager.renderBlock(
                    data.state,
                    data.pos,
                    world,
                    matrixStack,
                    buffer,
                    false,
                    world.random
            );

            matrixStack.pop();
        }

        matrixStack.pop();
        provider.draw();
    }

    static void renderOverlay(WorldRenderContext worldRenderContext) {
        if (!KeyBind.enabled) return;

        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        Frustum frustum = worldRenderContext.frustum();

        if (player == null || world == null || frustum == null) {
            return;
        }

        BlockPos playerPos = new BlockPos(player.getPos());

        // Render
        renderTranslucent(worldRenderContext, world, frustum, Compute.computeCache(playerPos, world));
    }
}

package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Render {
    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static final MinecraftClient client = MinecraftClient.getInstance();

    static void renderOverlay(WorldRenderContext worldRenderContext) {
        if (Lighty.mode == null) return;

        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        Frustum frustum = worldRenderContext.frustum();

        if (player == null || world == null || frustum == null) {
            return;
        }

        BlockPos playerPos = new BlockPos(player.getPos());
        // fixme: remove this from the render call
        Compute.computeCache(playerPos, world);

        // Render
        Lighty.mode.render(worldRenderContext, world, frustum, provider, client);
    }
}

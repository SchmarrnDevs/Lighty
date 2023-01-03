package dev.schmarrn.lighty;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;

public class Render {
    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static final MinecraftClient client = MinecraftClient.getInstance();

    static void renderOverlay(WorldRenderContext worldRenderContext) {
        if (Lighty.mode == null) return;

        ClientWorld world = client.world;
        Frustum frustum = worldRenderContext.frustum();

        if (world == null || frustum == null) {
            return;
        }

        // Render
        Lighty.mode.render(worldRenderContext, world, frustum, provider, client);
    }
}

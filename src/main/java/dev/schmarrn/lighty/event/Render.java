package dev.schmarrn.lighty.event;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;

public class Render {
    private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void renderOverlay(WorldRenderContext worldRenderContext) {
        LightyMode<?, ?> mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        ClientWorld world = client.world;
        Frustum frustum = worldRenderContext.frustum();

        if (world == null || frustum == null) {
            return;
        }

        // Render
        mode.render(worldRenderContext, world, frustum, provider, client);
    }

    private Render() {}
}

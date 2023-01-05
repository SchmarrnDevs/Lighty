package dev.schmarrn.lighty.mode;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public abstract class LightyMode {
    public void beforeCompute() {}

    public void afterCompute() {}

    public void unload() {}

    public abstract void compute(ClientWorld world, BlockPos pos);

    public abstract void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client);
}

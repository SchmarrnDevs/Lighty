package dev.schmarrn.lighty.mode;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public abstract class LightyMode {
    public static class Data {
        BlockPos pos;
        BlockState state;
        double offset;

        Data(BlockPos pos, BlockState state, double offset) {
            this.pos = pos;
            this.state = state;
            this.offset = offset;
        }
    }
    protected final List<Data> cache = Lists.newArrayList();

    public abstract void compute(ClientWorld world, BlockPos pos);

    public abstract void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client);

    public boolean shouldReCompute() {
        return cache.isEmpty();
    }

    public void clearCache() {
        cache.clear();
    }
}

package dev.schmarrn.lighty.api;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implement this class if you want to provide a LightyMode.
 */
public abstract class LightyMode {
    /**
     * If you need to do stuff before each compute-cycle.<br/>
     * A compute-cycle looks like this:<br/>
     * - exactly one call to `beforeCompute()`<br/>
     * - a bunch of `compute()` calls<br/>
     * - exactly one call to `afterCompute()`
     */
    public void beforeCompute() {}

    /**
     * If you need to do stuff after each compute-cycle.<br/>
     * A compute-cycle looks like this:<br/>
     * - exactly one call to `beforeCompute()`<br/>
     * - a bunch of `compute()` calls<br/>
     * - exactly one call to `afterCompute()`
     */
    public void afterCompute() {}

    /**
     * Implement the compute method to fill `cache` with the data you need for rendering.
     * This method gets called for each BlockPos that needs to be re-computed.
     *
     * @param world Provide Access to a `ClientWorld` instance.
     * @param pos The position that need re-computing.
     * @param builder Add your rendering stuff to this BufferBuilder
     */
    public abstract void compute(ClientWorld world, BlockPos pos, BufferBuilder builder);
}

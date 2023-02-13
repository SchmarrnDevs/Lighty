package dev.schmarrn.lighty.api;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implement this class if you want to provide a LightyMode.
 * Has a member called cache - use it to sync data between compute() and render().
 * See ModeCache for further details.
 *
 * @param <K> The Key of the Cache. Intended to be a BlockPos.
 * @param <V> Value of the Cache. Here you can store all the needed Data for your LightyMode.
 */
public abstract class LightyMode<K, V> {
    protected final ModeCache<K, V> cache = new ModeCache<>();

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
     * No LightyMode Implementation should ever need to unload itself.
     */
    @ApiStatus.Internal
    public void unload() {
        cache.clear();
    }

    /**
     * Lighty takes care of swapping the buffers for you, no need to swap it yourself.
     */
    @ApiStatus.Internal
    public void swap() {
        cache.swap();
    }

    /**
     * Implement the compute method to fill `cache` with the data you need for rendering.
     * This method gets called for each BlockPos that needs to be re-computed.
     *
     * @param world Provide Access to a `ClientWorld` instance.
     * @param pos The position that need re-computing.
     */
    public abstract void compute(ClientWorld world, BlockPos pos);

    /**
     * Implement the render method to display the data which you've stored in `cache` in `compute()`.<br/>
     * This method is most likely to change with Lighty 2.0.0
     *
     * @param worldRenderContext Access to camera and matrixStack
     * @param world useful for rendering blocks
     * @param frustum use frustum.isVisible to skip non visible blocks
     * @param provider you need to call provider.draw() at the end
     * @param client use the client to get different renderers
     */
    public abstract void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client);
}

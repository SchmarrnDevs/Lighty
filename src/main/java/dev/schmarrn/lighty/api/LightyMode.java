package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

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
    public void beforeCompute(BufferBuilder builder) {}

    public void beforeRendering() {}
    public void afterRendering() {}

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
    public abstract void compute(ClientLevel world, BlockPos pos, BufferBuilder builder);
}

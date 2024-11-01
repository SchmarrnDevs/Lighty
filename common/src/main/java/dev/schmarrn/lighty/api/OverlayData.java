package dev.schmarrn.lighty.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * Data that gets shared between the DataProvider and the Renderer.
 * @param valid ... whether the data is valid or not
 * @param color ... color that the resulting overlay should show
 * @param skyNumber ... sky light number of the block
 * @param blockNumber ... block light number of the block
 * @param pos ... position of the block
 * @param rPos ... relative position inside the chunk
 * @param yOffset ... how much to offset the overlay in y direction, useful for snow etc
 */
public record OverlayData(boolean valid, int color, int skyNumber, int blockNumber, BlockPos pos, Vec3i rPos, float yOffset) {
    public static OverlayData invalid() {
        return new OverlayData(false, 0, 0, 0, BlockPos.ZERO, BlockPos.ZERO, 0.0f);
    }
}

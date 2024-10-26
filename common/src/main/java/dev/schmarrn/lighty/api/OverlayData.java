package dev.schmarrn.lighty.api;

import net.minecraft.core.BlockPos;

/**
 * Data that gets shared between the DataProvider and the Renderer.
 * @param color ... color that the resulting overlay should show
 * @param skyNumber ... sky light number of the block
 * @param blockNumber ... block light number of the block
 * @param pos ... position of the block
 */
public record OverlayData(boolean valid, int color, int skyNumber, int blockNumber, BlockPos pos, float yOffset) {
    public static OverlayData invalid() {
        return new OverlayData(false, 0, 0, 0, BlockPos.ZERO, 0.0f);
    }
}

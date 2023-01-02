package dev.schmarrn.lighty;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;

public class Compute {
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;

    public static void computeCache(BlockPos playerPos, ClientWorld world, EntityType<?> type) {
        if (Lighty.mode == null) return;
        if (!oldPlayerPos.equals(playerPos)) {
            clearCache();
            oldPlayerPos = playerPos;
        }

        if (Lighty.mode.shouldReCompute()) {
            Lighty.LOGGER.debug("NEW COMPUTE");
            for (int x = -16; x <= 16; ++x) {
                for (int y = -16; y <= 16; ++y) {
                    for (int z = -16; z <= 16; ++z) {
                        Lighty.mode.compute(playerPos, world, type, x, y, z);
                    }
                }
            }
        }
    }

    public static void computeCache(BlockPos playerPos, ClientWorld world) {
        computeCache(playerPos, world, null);
    }

    public static void clearCache() {
        if (Lighty.mode != null)
            Lighty.mode.clearCache();
    }
}

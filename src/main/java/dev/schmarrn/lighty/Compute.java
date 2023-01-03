package dev.schmarrn.lighty;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Compute {
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;

    public static void computeCache(BlockPos playerPos, ClientWorld world) {
        if (Lighty.mode == null) return;
        if (!oldPlayerPos.equals(playerPos)) {
            clearCache();
            oldPlayerPos = playerPos;
        }

        if (Lighty.mode.shouldReCompute()) {
            for (int x = -16; x <= 16; ++x) {
                for (int y = -16; y <= 16; ++y) {
                    for (int z = -16; z <= 16; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        Lighty.mode.compute(world, pos);
                    }
                }
            }
        }
    }

    public static void clearCache() {
        if (Lighty.mode != null)
            Lighty.mode.clearCache();
    }
}

package dev.schmarrn.lighty;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Compute {
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;
    private static boolean isDirty = true;

    public static void computeCache(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        BlockPos playerPos = player.getBlockPos();

        if (Lighty.mode == null) return;
        if (!oldPlayerPos.equals(playerPos)) {
            markDirty();
            oldPlayerPos = playerPos;
        }

        if (isDirty) {
            isDirty = false;
            Lighty.mode.beforeCompute();
            for (int x = -16; x <= 16; ++x) {
                for (int y = -16; y <= 16; ++y) {
                    for (int z = -16; z <= 16; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        Lighty.mode.compute(world, pos);
                    }
                }
            }
            Lighty.mode.afterCompute();
        }
    }

    public static void markDirty() {
        isDirty = true;
    }
}

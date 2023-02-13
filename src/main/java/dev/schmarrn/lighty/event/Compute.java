package dev.schmarrn.lighty.event;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Compute {
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;
    private static boolean isDirty = true;
    public static void markDirty() {
        isDirty = true;
    }

    private static int computeDistance = 16;

    public static void setComputeDistance(int distance) {
        computeDistance = distance;
    }

    public static void computeCache(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        BlockPos playerPos = player.getBlockPos();

        LightyMode<?, ?> mode = ModeLoader.getCurrentMode();

        if (mode == null) return;
        if (!oldPlayerPos.equals(playerPos)) {
            markDirty();
            oldPlayerPos = playerPos;
        }

        if (isDirty) {
            isDirty = false;
            mode.beforeCompute();
            for (int x = -computeDistance; x <= computeDistance; ++x) {
                for (int y = -computeDistance; y <= computeDistance; ++y) {
                    for (int z = -computeDistance; z <= computeDistance; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        mode.compute(world, pos);
                    }
                }
            }
            mode.afterCompute();
            mode.swap();
        }
    }

    private Compute() {}
}

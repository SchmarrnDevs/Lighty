package dev.schmarrn.lighty;

import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

public class Compute {
    public static class OverlayData {
        BlockPos pos;
        BlockState state;
        double offset;

        OverlayData(BlockPos pos, BlockState state, double offset) {
            this.pos = pos;
            this.state = state;
            this.offset = offset;
        }
    }

    public static boolean isBlocked(BlockState block, BlockState up) {
        // If block with FluidState (think Kelp, Seagrass, Glowlichen underwater), disable overlay
        return (!up.getFluidState().isEmpty()) ||
                // MagmaBlocks caused a Crash - But Mobs can still spawn on them, I need to fix this
                block.getBlock() instanceof MagmaBlock;
    }

    private static final List<OverlayData> cache = new ArrayList<>();
    private static BlockPos oldPlayerPos = BlockPos.ORIGIN;

    public static List<OverlayData> computeCache(BlockPos playerPos, ClientWorld world, EntityType<?> type) {
        if (!oldPlayerPos.equals(playerPos)) {
            cache.clear();
        }
        oldPlayerPos = playerPos;

        if (cache.isEmpty()) {
            Lighty.LOGGER.debug("NEW COMPUTE");
            for (int x = -16; x <= 16; ++x) {
                for (int y = -16; y <= 16; ++y) {
                    for (int z = -16; z <= 16; ++z) {
                        BlockPos pos = new BlockPos(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                        BlockPos posUp = pos.up();
                        BlockState up = world.getBlockState(posUp);
                        Block upBlock = up.getBlock();
                        BlockState block = world.getBlockState(pos);
                        boolean validSpawn = upBlock.canMobSpawnInside();
                        if (isBlocked(block, up)) {
                            continue;
                        }
                        validSpawn = validSpawn && world.getBlockState(pos).allowsSpawning(world, pos, type);

                        if (!validSpawn) {
                            continue;
                        }

                        int blockLightLevel = world.getLightLevel(LightType.BLOCK, posUp);
                        int skyLightLevel = world.getLightLevel(LightType.SKY, posUp);

                        BlockState overlayState = Blocks.GREEN_OVERLAY.getDefaultState();
                        if (blockLightLevel == 0) {
                            if (skyLightLevel == 0) {
                                overlayState = Blocks.RED_OVERLAY.getDefaultState();
                            } else {
                                overlayState = Blocks.ORANGE_OVERLAY.getDefaultState();
                            }
                        }

                        double offset = 0;
                        if (upBlock instanceof SnowBlock) { // snow layers
                            int layer = world.getBlockState(posUp).get(SnowBlock.LAYERS);
                            // One layer of snow is two pixels high, with one pixel being 1/16
                            offset = 2f / 16f * layer;
                        } else if (upBlock instanceof CarpetBlock) {
                            // Carpet is just one pixel high
                            offset = 1f / 16f;
                        }

                        cache.add(new OverlayData(posUp, overlayState, offset));
                    }
                }
            }
        }

        return cache;
    }

    public static List<OverlayData> computeCache(BlockPos playerPos, ClientWorld world) {
        return computeCache(playerPos, world, null);
    }

    public static void clearCache() {
        cache.clear();
    }
}

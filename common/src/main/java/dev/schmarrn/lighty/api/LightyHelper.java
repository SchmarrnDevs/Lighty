package dev.schmarrn.lighty.api;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

public class LightyHelper {
    static HashSet<ResourceLocation> invalidBlocks = new HashSet<>();

    private static boolean isRedstone(Block block) {
        return block instanceof RedStoneWireBlock ||
                block instanceof ButtonBlock ||
                block instanceof PressurePlateBlock ||
                block instanceof LeverBlock;
    }

    private static boolean protectedIsValidSpawnCheck(BlockState block, BlockPos pos, ClientLevel world) {
        // One exception is magma, because that predicate uses the entity without null check
        if (block.getBlock() instanceof MagmaBlock) {
            return true;
        } else {
            try {
                return block.isValidSpawn(world, pos, null);
            } catch (NullPointerException e) {
                ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block.getBlock());
                if (!invalidBlocks.contains(rl)) {
                    invalidBlocks.add(rl);
                    Lighty.LOGGER.error(e.getMessage());
                    Lighty.LOGGER.error("Cannot check `isValidSpawn` on Block {} because it uses entity checks. The overlay might not be accurate for that block.", rl);
                }
                return true;
            }
        }
    }

    private static boolean specialCases(Block block) {
        return block instanceof CarpetBlock;
    }

    public static float getOffset(BlockState block, BlockPos pos, ClientLevel world) {
        float offset = 0;
        if (block.getBlock() instanceof SnowLayerBlock) { // snow layers
            int layer = world.getBlockState(pos).getValue(SnowLayerBlock.LAYERS);
            if (layer != 1) {
                // Mobs cannot spawn on those blocks
                return -1;
            }
            // One layer of snow is two pixels high, with one pixel being 1/16
            offset = 2f / 16f * layer;
        }
        return offset;
    }

    public static boolean isBlocked(BlockState block, BlockState up, ClientLevel world, BlockPos pos, BlockPos upPos) {
        // Resource: https://minecraft.fandom.com/wiki/Tutorials/Spawn-proofing
        return (up.isCollisionShapeFullBlock(world, upPos) || // Full blocks are not spawnable in
                !block.isFaceSturdy(world, pos, Direction.UP) || // Block below needs to be sturdy
                isRedstone(up.getBlock()) || // Mobs don't spawn in redstone
                specialCases(up.getBlock()) || // Carpets and snow
                !protectedIsValidSpawnCheck(block, pos, world) || // use minecraft internal isValidSpawn check
                !up.getFluidState().isEmpty()) || // don't spawn in fluidlogged stuff (Kelp, Seagrass, Growlichen)
                !up.getBlock().isPossibleToRespawnInThis(up) ||
                up.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE); // As of 1.20.1, only contains rails, don't know if it is even really available on the client
    }

    public static boolean isSafe(int blockLightLevel) {
        return !(blockLightLevel <= Config.getBlockThreshold());
    }
}

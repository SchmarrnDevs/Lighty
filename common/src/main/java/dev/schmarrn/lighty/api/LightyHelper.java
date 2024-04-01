package dev.schmarrn.lighty.api;

import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class LightyHelper {
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
            return block.isValidSpawn(world, pos, null);
        }
    }

    private static boolean specialCases(Block block) {
        return block instanceof CarpetBlock;
    }

    public static float getOffset(BlockState blockState, BlockPos pos, BlockState blockStateUp, BlockPos posUp, ClientLevel world) {
        // Returns the offset of blocks that aren't 16 pixels high, for example snow layers or farmland.
        Block blockUp = blockStateUp.getBlock();
        if (blockUp instanceof SnowLayerBlock) { // snow layers
            int layer = world.getBlockState(posUp).getValue(SnowLayerBlock.LAYERS);
            if (layer != 1) {
                // Mobs cannot spawn on those blocks
                return -1;
            }
            // One layer of snow is two pixels high, with one pixel being 1/16
            return 2f / 16f * layer;
        }

        Block block = blockState.getBlock();
        if (block instanceof FarmBlock) { // farmland
            // FarmBlocks are 15 pixels high
            return -1f/16f;
        }

        return 0f;
    }

    public static boolean isBlocked(BlockState block, BlockState up, ClientLevel world, BlockPos pos, BlockPos upPos) {
        if (block.getBlock() instanceof FarmBlock) return false; // farmland

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

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

    public static float getOffset(BlockState blockState, BlockPos pos, ClientLevel world) {
        // Returns the offset of blocks that aren't 16 pixels high
        BlockState blockStateUp = world.getBlockState(pos.above());
        Block blockUp = blockStateUp.getBlock();
        if (blockUp instanceof SnowLayerBlock) { // snow layers
            int layer = blockStateUp.getValue(SnowLayerBlock.LAYERS);
            if (layer != 1) {
                // Mobs cannot spawn on those blocks
                return -1;
            }
            // One layer of snow is two pixels high, with one pixel being 1/16
            return 2f / 16f * layer;
        }

        return 0f;
    }

    public static boolean isBlocked(BlockState block, BlockPos pos, ClientLevel world) {
        BlockPos posUp = pos.above();
        BlockState blockStateUp = world.getBlockState(posUp);
        // Resource: https://minecraft.fandom.com/wiki/Tutorials/Spawn-proofing
        return (blockStateUp.isCollisionShapeFullBlock(world, posUp) || // Full blocks are not spawnable in
                !block.isFaceSturdy(world, pos, Direction.UP) || // Block below needs to be sturdy
                isRedstone(blockStateUp.getBlock()) || // Mobs don't spawn in redstone
                specialCases(blockStateUp.getBlock()) || // Carpets and snow
                !protectedIsValidSpawnCheck(block, pos, world) || // use minecraft internal isValidSpawn check
                !blockStateUp.getFluidState().isEmpty()) || // don't spawn in fluidlogged stuff (Kelp, Seagrass, Growlichen)
                !blockStateUp.getBlock().isPossibleToRespawnInThis(blockStateUp) ||
                blockStateUp.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE); // As of 1.20.1, only contains rails, don't know if it is even really available on the client
    }

    public static boolean isSafe(int blockLightLevel) {
        return !(blockLightLevel <= Config.BLOCK_THRESHOLD.getValue());
    }
}

package dev.schmarrn.lighty.mode.carpet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OverlayBlock extends Block {
    public final int color;

    public OverlayBlock(BlockBehaviour.Properties settings, int color) {
        super(settings.noCollission().noOcclusion());

        this.color = color;
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Blocks.RED_CARPET.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
    }
}

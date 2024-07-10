package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class BlockPlacedMixin {
    @Inject(method = "blockChanged", at = @At("HEAD"))
    private void lighty$onPlace(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2, int i, CallbackInfo ci) {
        // TODO Nice to have: Only execute on blocks that don't change the lightmap
        // On the other Hand: We are just inserting into a HashMap, which should be cheap enough to don't care
        Compute.updateBlockPos(blockPos);
    }
}

package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class BlockPlacedMixin {
    @Inject(method = "setBlock", at = @At("HEAD"))
    private void lighty$onPlace(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        // TODO Nice to have: Only execute on blocks that don't change the lightmap
        // On the other Hand: We are just inserting into a HashMap, which should be cheap enough to don't care
        Compute.updateSubChunk(SectionPos.of(blockPos));
    }
}

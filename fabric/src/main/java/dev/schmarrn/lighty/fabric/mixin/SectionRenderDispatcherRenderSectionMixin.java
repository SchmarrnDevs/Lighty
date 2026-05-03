package dev.schmarrn.lighty.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.schmarrn.lighty.fabric.api.AddSectionGeometryEvent;
import dev.schmarrn.lighty.fabric.api.AdditionalSectionRenderers;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/// Entrypoint for the AdditionalSectionRenderer event.
@Mixin(SectionRenderDispatcher.RenderSection.class)
public class SectionRenderDispatcherRenderSectionMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos renderOrigin;

    @Shadow
    @Final
    SectionRenderDispatcher this$0;

    @Inject(
            method = "createCompileTask",
            at= @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/atomic/AtomicReference;get()Ljava/lang/Object;"
            )
    )
    public void lighty$createCompileTask(RenderRegionCache cache, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection.CompileTask> cir, @Local(name = "region") RenderSectionRegion region) {
        List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers = new ArrayList<>();
        AddSectionGeometryEvent.EVENT.invoker().run(additionalRenderers, renderOrigin, this$0.level);
        if (!additionalRenderers.isEmpty()) {
            ((AdditionalSectionRenderers) region).lighty$setAdditionalRenderers(additionalRenderers);
        }
    }
}

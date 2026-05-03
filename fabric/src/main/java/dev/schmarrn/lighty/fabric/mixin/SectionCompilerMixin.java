package dev.schmarrn.lighty.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.schmarrn.lighty.fabric.api.AddSectionGeometryEvent;
import dev.schmarrn.lighty.fabric.api.AdditionalSectionRenderers;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/// Injecting into the SectionCompiler to run our additional renderers.
/// The additional renderers are carried via RenderSectionRegion.
@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> startedLayers, SectionBufferBuilderPack buffers, ChunkSectionLayer layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;")
    )
    void lighty$compile(SectionPos sectionPos, RenderSectionRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack builders, CallbackInfoReturnable<SectionCompiler.Results> cir, @Local(name = "blockRenderer") ModelBlockRenderer blockRenderer, @Local(name = "startedLayers") Map<ChunkSectionLayer, BufferBuilder> startedLayers) {
        var additionalRenderers = ((AdditionalSectionRenderers) region).lighty$getAdditionalRenderers();
        if (additionalRenderers == null || additionalRenderers.isEmpty()) {
            return;
        }
        final var context = new AddSectionGeometryEvent.SectionRenderingContext((layer) -> getOrBeginLayer(startedLayers, builders, layer), region, blockRenderer);

        for (final var renderer : additionalRenderers) {
            renderer.render(context);
        }
    }
}

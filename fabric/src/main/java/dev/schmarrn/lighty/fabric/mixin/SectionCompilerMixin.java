package dev.schmarrn.lighty.fabric.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.schmarrn.lighty.fabric.AddSectionGeometryEvent;
import dev.schmarrn.lighty.fabric.LightyFabric;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> startedLayers, SectionBufferBuilderPack buffers, ChunkSectionLayer layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    void lighty$compile(SectionPos sectionPos, RenderSectionRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack builders, CallbackInfoReturnable<SectionCompiler.Results> cir, SectionCompiler.Results results, BlockPos minPos, BlockPos maxPos, VisGraph visGraph, ModelBlockRenderer blockRenderer, FluidRenderer fluidRenderer, Map startedLayers) {
        var additionalRenderers = LightyFabric.CACHE.get(sectionPos);
        if (additionalRenderers == null || additionalRenderers.isEmpty()) {
            return;
        }
        final var context = new AddSectionGeometryEvent.SectionRenderingContext((layer) -> getOrBeginLayer(startedLayers, builders, layer), region, blockRenderer);

        for (final var renderer : additionalRenderers) {
            renderer.render(context);
        }
    }
}

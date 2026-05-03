package dev.schmarrn.lighty.fabric.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.schmarrn.lighty.fabric.api.AddSectionGeometryEvent;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.fabric.level.FabricLevelRenderHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/// Implement the FabricLevelRenderHooks for my custom implementation of the AdditionalSectionRenderers NeoForge API.
/// This WILL BREAK once Sodium implements something for Fabric here.
/// The implementation is mostly ported from NeoForgeLevelRenderHooks.
@Pseudo
@Mixin(FabricLevelRenderHooks.class)
public class SodiumMixin {
    @Inject(method = "retrieveChunkMeshAppenders", at = @At("HEAD"), cancellable = true)
    void lighty$runChunkLayerEvents(Level level, BlockPos origin, CallbackInfoReturnable<List<?>> cir) {
        List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers = new ArrayList<>();
        AddSectionGeometryEvent.EVENT.invoker().run(additionalRenderers, origin, level);
        cir.setReturnValue(additionalRenderers);
    }

    @Inject(method = "runChunkMeshAppenders", at = @At("HEAD"))
    void lighty$runChunkMeshAppenders(List<?> renderers, Function<ChunkSectionLayer, VertexConsumer> typeToConsumer, LevelSlice slice, BlockPos origin, CallbackInfo ci) {
        if (renderers == null || renderers.isEmpty()) {
            return;
        }
        final var context = new AddSectionGeometryEvent.SectionRenderingContext(
                typeToConsumer,
                slice,
                new ModelBlockRenderer(
                    Minecraft.getInstance().options.ambientOcclusion().get(),
                    true,
                    Minecraft.getInstance().getBlockColors()
                )
        );

        for (final var renderer : renderers) {
            ((AddSectionGeometryEvent.AdditionalSectionRenderer) renderer).render(context);
        }

    }
}

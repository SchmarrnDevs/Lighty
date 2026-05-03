package dev.schmarrn.lighty.fabric.mixin;

import dev.schmarrn.lighty.fabric.api.AddSectionGeometryEvent;
import dev.schmarrn.lighty.fabric.api.AdditionalSectionRenderers;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/// Adding a List of `AdditionalSectionRenderer`s to `RenderSectionRegion`, as that data is passed all the way to the compiler
@Mixin(RenderSectionRegion.class)
public class RenderSectionRegionMixin implements AdditionalSectionRenderers {
    @Unique
    List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalSectionRenderers = null;

    @Override
    public List<AddSectionGeometryEvent.AdditionalSectionRenderer> lighty$getAdditionalRenderers() {
        return additionalSectionRenderers;
    }

    @Override
    public void lighty$setAdditionalRenderers(List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalSectionRenderers) {
        this.additionalSectionRenderers = additionalSectionRenderers;
    }
}

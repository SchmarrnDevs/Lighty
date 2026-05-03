package dev.schmarrn.lighty.fabric.api;

import java.util.List;

public interface AdditionalSectionRenderers {
    List<AddSectionGeometryEvent.AdditionalSectionRenderer> lighty$getAdditionalRenderers();
    void lighty$setAdditionalRenderers(List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalSectionRenderers);
}

package dev.schmarrn.lighty.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface OverlayDataProvider {
    OverlayData compute(ClientLevel level, BlockPos pos);

    ResourceLocation getResourceLocation();
}

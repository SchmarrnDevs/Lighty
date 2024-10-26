package dev.schmarrn.lighty.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public interface OverlayDataProvider {
    OverlayData compute(ClientLevel level, BlockPos pos);
}

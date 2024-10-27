package dev.schmarrn.lighty.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public interface OverlayRenderer {
    void build(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap);

    void beforeRendering();
    void afterRendering();
}

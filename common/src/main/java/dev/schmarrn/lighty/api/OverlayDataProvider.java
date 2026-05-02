package dev.schmarrn.lighty.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.LevelChunk;

public interface OverlayDataProvider {
    OverlayData compute(ClientLevel level, LevelChunk chunk, BlockPos pos, Vec3i rPos);

    OverlayRenderer getRenderer();

    Identifier getIdentifier();
}

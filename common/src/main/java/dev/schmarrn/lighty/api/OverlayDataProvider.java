package dev.schmarrn.lighty.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.LevelChunk;

public interface OverlayDataProvider {
    @Deprecated
    default OverlayData compute(ClientLevel level, BlockPos pos, Vec3i rPos) {
        return OverlayData.INVALID;
    }

    default OverlayData compute(ClientLevel level, LevelChunk chunk, BlockPos pos, Vec3i rPos) {
        return this.compute(level, pos, rPos);
    }

    OverlayRenderer getRenderer();

    Identifier getIdentifier();
}

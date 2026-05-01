package dev.schmarrn.lighty.dataproviders;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.*;
import dev.schmarrn.lighty.core.RendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class FarmlandDataProvider implements OverlayDataProvider {
    @Override
    public OverlayData compute(ClientLevel level, BlockPos pos, Vec3i rPos) {
        return compute(level, level.getChunkAt(pos), pos, rPos);
    }

    public OverlayData compute(ClientLevel level, LevelChunk chunk, BlockPos pos, Vec3i rPos) {
        BlockState blockState = chunk.getBlockState(pos);
        if (!(blockState.getBlock() instanceof FarmlandBlock)) {
            return OverlayData.INVALID;
        }

        BlockPos posUp = pos.above();

        int blockLightLevel = level.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = level.getBrightness(LightLayer.SKY, posUp);

        int color = LightyColors.getGrowthARGB(blockLightLevel, skyLightLevel);

        float offset = -1f/15f;

        return new OverlayData(true, color, skyLightLevel, blockLightLevel, pos, rPos, offset);
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "data_provider_farmland");
    }

    @Override
    public OverlayRenderer getRenderer() {
        return RendererRegistry.getRenderer();
    }

    public static void init() {
        var dp = new FarmlandDataProvider();
        ModeManager.registerDataProvider(dp.getIdentifier(), dp);
    }
}

package dev.schmarrn.lighty.dataproviders;

import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FarmlandDataProvider implements OverlayDataProvider {
    public OverlayData compute(ClientLevel level, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState blockState = level.getBlockState(pos);

        if (!(blockState.getBlock() instanceof FarmBlock)) {
            return OverlayData.invalid();
        }

        int blockLightLevel = level.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = level.getBrightness(LightLayer.SKY, posUp);

        int color = LightyColors.getGrowthARGB(blockLightLevel, skyLightLevel);

        float offset = -1f/15f;

        return new OverlayData(true, color, skyLightLevel, blockLightLevel, pos, offset);
    }
}

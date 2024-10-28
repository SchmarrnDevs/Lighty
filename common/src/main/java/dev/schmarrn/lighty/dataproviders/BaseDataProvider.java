package dev.schmarrn.lighty.dataproviders;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.*;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class BaseDataProvider implements OverlayDataProvider {
    public OverlayData compute(ClientLevel level, BlockPos pos) {
        BlockPos posUp = pos.above();
        BlockState blockState = level.getBlockState(pos);

        if (LightyHelper.isBlocked(blockState, pos, level)) {
            return OverlayData.invalid();
        }

        int blockLightLevel = level.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = level.getBrightness(LightLayer.SKY, posUp);

        if (LightyHelper.isSafe(blockLightLevel) && !Config.SHOW_SAFE.getValue()) {
            return OverlayData.invalid();
        }

        int color = LightyColors.getARGB(blockLightLevel, skyLightLevel);

        float offset = LightyHelper.getOffset(blockState, pos, level);
        if (offset == -1f) {
            return OverlayData.invalid();
        }

        return new OverlayData(true, color, skyLightLevel, blockLightLevel, pos, offset);
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "data_provider_base");
    }

    public static void init() {
        var dp = new BaseDataProvider();
        ModeManager.registerDataProvider(dp.getResourceLocation(), dp);
    }
}

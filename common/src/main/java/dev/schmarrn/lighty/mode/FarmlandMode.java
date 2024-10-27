package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.dataproviders.FarmlandDataProvider;
import dev.schmarrn.lighty.dataproviders.NormalDataProvider;
import dev.schmarrn.lighty.renderers.CarpetRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extending CarpetMode because the code is largely identical
 */
public class FarmlandMode extends CarpetMode {
    private static final FarmlandDataProvider dataProvider = new FarmlandDataProvider();
    private static final CarpetRenderer renderer = new CarpetRenderer();

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        OverlayData data = dataProvider.compute(world, pos);

        if (data.valid()) {
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            renderer.build(world, pos, data, builder, lightmap);
        }
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "farmland_mode");
    }

    public static void init() {
        FarmlandMode mode = new FarmlandMode();
        ModeManager.registerMode(mode.getResourceLocation(), mode);
    }
}
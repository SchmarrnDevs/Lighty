// Copyright 2022-2023 The Lighty contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.*;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.dataproviders.NormalDataProvider;
import dev.schmarrn.lighty.renderers.CrossRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BoringCrossMode extends LightyMode {
    private static final NormalDataProvider dataProvider = new NormalDataProvider();
    private static final CrossRenderer renderer = new CrossRenderer();

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        OverlayData data = dataProvider.compute(world, pos);

        if (data.valid()) {
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            renderer.compute(world, pos, data, builder, lightmap);
        }
    }

    @Override
    public void beforeRendering() {
        RenderType.cutout().setupRenderState();
        RenderSystem.setShaderTexture(0, Config.CROSS_TEXTURE.getValue());
        RenderSystem.enableDepthTest();
    }

    @Override
    public void afterRendering() {
        RenderSystem.disableDepthTest();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "boring_cross_mode");
    }

    public static void init() {
        BoringCrossMode mode = new BoringCrossMode();
        ModeManager.registerMode(mode.getResourceLocation(), mode);
    }
}

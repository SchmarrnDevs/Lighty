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
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyHelper;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class BoringCrossMode extends LightyMode {
    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockState blockState = world.getBlockState(pos);
        if (!LightyHelper.isBlocked(blockState, pos, world)) {
            int blockLightLevel = world.getBrightness(LightLayer.BLOCK, pos.above());
            int skyLightLevel = world.getBrightness(LightLayer.SKY, pos.above());

            if (LightyHelper.isSafe(blockLightLevel) && !Config.SHOW_SAFE.getValue()) {
                return;
            }

            int color = LightyColors.getARGB(blockLightLevel, skyLightLevel);

            float offset = LightyHelper.getOffset(blockState, pos, world);
            if (offset == -1f) {
                return;
            }

            float x1 = pos.getX();
            float x2 = pos.getX() + 1f;
            float y = pos.getY() + 1.005f + offset;
            float z1 = pos.getZ();
            float z2 = pos.getZ() + 1f;

            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);

            builder.addVertex(x1, y, z1).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x1, y, z2).setColor(color).setUv(0, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x2, y, z2).setColor(color).setUv(1, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x2, y, z1).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
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

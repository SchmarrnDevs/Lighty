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
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;

public class BoringCrossMode extends LightyMode {

    @Override
    public void beforeCompute(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        if (world.getBlockState(pos).isAir() && !world.getBlockState(pos.below()).isAir()) {
            int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
            int skyLight = world.getBrightness(LightLayer.SKY, pos);

            int data = LightyColors.getSafeARGB();

            if (blockLight <= Config.getBlockThreshold()) {
                if (skyLight <= Config.getSkyThreshold()) {
                    data = LightyColors.getDangerARGB();
                } else {
                    data = LightyColors.getWarningARGB();
                }
            }
            float x1 = pos.getX();
            float x2 = pos.getX() + 1f;
            float y = pos.getY() + 0.005f;
            float z1 = pos.getZ();
            float z2 = pos.getZ() + 1f;

            builder.vertex(x1, y, z1).color(data).endVertex();
            builder.vertex(x2, y, z2).color(data).endVertex();
            builder.vertex(x2, y, z1).color(data).endVertex();
            builder.vertex(x1, y, z2).color(data).endVertex();
        }
    }

    @Override
    public void beforeRendering() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void afterRendering() {
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.0F);
    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "boring_cross_mode"), new BoringCrossMode());
    }
}

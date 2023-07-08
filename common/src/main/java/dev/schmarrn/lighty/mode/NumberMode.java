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
import dev.schmarrn.lighty.api.LightyHelper;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class NumberMode extends LightyMode {
    @Override
    public void beforeCompute(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    private static final float PXL = 1/16f;
    private static final float dx = 0.25f;
    private static final float dz = 0.25f;

    private static void renderDigit(BufferBuilder builder, int digit, float x, float y, float z, int color) {
        float startU = (0b11 & digit) / 4f;
        float startV = ((digit >> 2) & 0b11) / 4f;

        builder.vertex(x, y, z).uv(startU,startV).color(color).endVertex();
        builder.vertex(x, y, z + dz).uv(startU, startV + 0.25f).color(color).endVertex();
        builder.vertex(x + dx, y, z + dz).uv(startU + 0.25f, startV + 0.25f).color(color).endVertex();
        builder.vertex(x + dx, y, z).uv(startU + 0.25f, startV).color(color).endVertex();
    }

    private static void renderNumber(BufferBuilder builder, int number, float x, float y, float z, int color) {
        int oneDigit = number % 10;
        int tenDigit = number / 10;

        if (tenDigit > 0) {
            renderDigit(builder, tenDigit, x, y, z, color);
            renderDigit(builder, oneDigit, x + dx - PXL, y, z, color);
        } else {
            renderDigit(builder, oneDigit, x + (dx - PXL)/2f, y, z, color);
        }
    }

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockPos posUp = pos.above();
        BlockState up = world.getBlockState(posUp);
        BlockState block = world.getBlockState(pos);
        if (LightyHelper.isBlocked(block, up, world, pos, posUp)) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);

        int color = LightyColors.getARGB(blockLightLevel, skyLightLevel);

        float offset = LightyHelper.getOffset(up, posUp, world);
        if (offset == -1f) {
            return;
        }

        float x1 = pos.getX() + PXL * 5.25f;
        float y = pos.getY() + 1f + 0.005f + offset;
        float z1 = pos.getZ() + PXL * 4f;

        renderNumber(builder, blockLightLevel, x1, y, z1, color);
        renderNumber(builder, skyLightLevel, x1, y, z1 + 0.3f, color);
    }

    @Override
    public void beforeRendering() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, new ResourceLocation(Lighty.MOD_ID, "textures/block/numbers.png"));
    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "number_mode"), new NumberMode());
    }
}
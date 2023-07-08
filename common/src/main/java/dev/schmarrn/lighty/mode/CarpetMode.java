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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarpetMode extends LightyMode {
    @Override
    public void beforeCompute(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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

        double offset = LightyHelper.getOffset(up, posUp, world);

        if (offset == -1f) {
            return;
        }

        double x = pos.getX();
        double y = pos.getY() + 1 + offset;
        double z = pos.getZ();
        //TOP
        builder.vertex(x, y + 1 / 16f, z).uv(0, 0).color(color).endVertex();
        builder.vertex(x, y + 1 / 16f, z + 1).uv(0, 1).color(color).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z + 1).uv(1, 1).color(color).endVertex();
        builder.vertex(x + 1, y + 1 / 16f, z).uv(1, 0).color(color).endVertex();
        if (offset > 0.001f) {
            //if it renders above it should check if the block above culls the faces
            pos = pos.above();
        }
        //NORTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.SOUTH, pos.relative(Direction.SOUTH))) {
            builder.vertex(x, y + 1 / 16f, z + 1).uv(0, 1f / 16).color(color).endVertex();
            builder.vertex(x, y, z + 1).uv(0, 0).color(color).endVertex();
            builder.vertex(x + 1, y, z + 1).uv(1, 0).color(color).endVertex();
            builder.vertex(x + 1, y + 1 / 16f, z + 1).uv(1, 1f / 16).color(color).endVertex();
        }
        //EAST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.WEST, pos.relative(Direction.WEST))) {
            builder.vertex(x, y + 1/16f, z).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x, y, z).uv(0, 0).color(color).endVertex();
            builder.vertex(x, y, z + 1).uv(1, 0).color(color).endVertex();
            builder.vertex(x, y + 1/16f, z + 1).uv(1, 1f/16).color(color).endVertex();
        }
        //SOUTH
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.NORTH, pos.relative(Direction.NORTH))) {
            builder.vertex(x+1, y + 1/16f, z).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x+1, y, z).uv(0, 0).color(color).endVertex();
            builder.vertex(x, y, z).uv(1, 0).color(color).endVertex();
            builder.vertex(x, y + 1/16f, z).uv(1, 1f/16).color(color).endVertex();
        }
        //WEST
        if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world, pos, Direction.EAST, pos.relative(Direction.EAST))) {
            builder.vertex(x+1, y + 1/16f, z+1).uv(0,1f/16).color(color).endVertex();
            builder.vertex(x+1, y, z+1).uv(0, 0).color(color).endVertex();
            builder.vertex(x+1, y, z).uv(1, 0).color(color).endVertex();
            builder.vertex(x+1, y + 1/16f, z).uv(1, 1f/16).color(color).endVertex();
        }
    }

    @Override
    public void beforeRendering() {
        RenderType.translucent().setupRenderState();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(Lighty.MOD_ID, "textures/block/transparent.png"));
        RenderSystem.enableDepthTest();
    }

    @Override
    public void afterRendering() {
        RenderType.translucent().clearRenderState();
        RenderSystem.disableDepthTest();
    }
    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "carpet_mode"), new CarpetMode());
    }
}

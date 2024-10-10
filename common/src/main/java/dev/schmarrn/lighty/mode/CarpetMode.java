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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarpetMode extends LightyMode {
    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        BlockPos posUp = pos.above();
        BlockState blockState = world.getBlockState(pos);

        if (LightyHelper.isBlocked(blockState, pos, world)) {
            return;
        }

        int blockLightLevel = world.getBrightness(LightLayer.BLOCK, posUp);
        int skyLightLevel = world.getBrightness(LightLayer.SKY, posUp);

        if (LightyHelper.isSafe(blockLightLevel) && !Config.SHOW_SAFE.getValue()) {
            return;
        }

        int color = LightyColors.getARGB(blockLightLevel, skyLightLevel);

        float offset = LightyHelper.getOffset(blockState, pos, world);
        if (offset == -1f) {
            return;
        }

        float x = pos.getX();
        float y = pos.getY() + 1 + offset;
        float z = pos.getZ();
        int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
        // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
        int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
        //TOP
        try {
            builder.addVertex(x, y + 1 / 16f, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x, y + 1 / 16f, z + 1).setColor(color).setUv(0, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(color).setUv(1, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
            builder.addVertex(x + 1, y + 1 / 16f, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
            if (offset > 0.001f) {
                //if it renders above it should check if the block above culls the faces
                pos = pos.above();
            }
            //NORTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.SOUTH)), Direction.SOUTH)) {
                builder.addVertex(x, y + 1 / 16f, z + 1).setColor(color).setUv(0, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z + 1).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y, z + 1).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x + 1, y + 1 / 16f, z + 1).setColor(color).setUv(1, 1f / 16).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //EAST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.WEST)), Direction.WEST)) {
                builder.addVertex(x, y + 1/16f, z).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y, z + 1).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(-1f, 0f, 0f);
                builder.addVertex(x, y + 1/16f, z + 1).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(-1f, 0f, 0f);
            }
            //SOUTH
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.NORTH)), Direction.NORTH)) {
                builder.addVertex(x+1, y + 1/16f, z).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(0f, 0f, 1f);
                builder.addVertex(x+1, y, z).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(0f, 0f, -1f);
                builder.addVertex(x, y + 1/16f, z).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(0f, 0f, -1f);
            }
            //WEST
            if (Block.shouldRenderFace(Blocks.STONE.defaultBlockState(), world.getBlockState(pos.relative(Direction.EAST)), Direction.EAST)) {
                builder.addVertex(x+1, y + 1/16f, z+1).setColor(color).setUv(0,1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z+1).setColor(color).setUv(0, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y, z).setColor(color).setUv(1, 0).setLight(lightmap).setNormal(1f, 0f, 0f);
                builder.addVertex(x+1, y + 1/16f, z).setColor(color).setUv(1, 1f/16).setLight(lightmap).setNormal(1f, 0f, 0f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeRendering() {
        RenderType.translucent().setupRenderState();
        RenderSystem.setShaderTexture(0, Config.CARPET_TEXTURE.getValue());
        RenderSystem.enableDepthTest();
    }

    @Override
    public void afterRendering() {
        RenderType.translucent().clearRenderState();
        RenderSystem.disableDepthTest();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "carpet_mode");
    }

    public static void init() {
        CarpetMode mode = new CarpetMode();
        ModeManager.registerMode(mode.getResourceLocation(), mode);
    }
}

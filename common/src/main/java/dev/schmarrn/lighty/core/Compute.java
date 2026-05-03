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

package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.overlaystate.SMACH;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class Compute {
    /// Private copy of the player's currently occupied section.
    /// Used to mark SectionPoses around the player as dirty.
    /// Updated each tick.
    private static SectionPos playerPos = SectionPos.of(0,0,0);

    public static void markDirty() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        int renderDistance = minecraft.options.renderDistance().get();
        minecraft.levelRenderer.setSectionRangeDirty(
                playerPos.x() - renderDistance,
                level.getMinSectionY(),
                playerPos.z() - renderDistance,
                playerPos.x() + renderDistance,
                level.getMaxSectionY(),
                playerPos.z() + renderDistance
        );
    }

    public static void tick() {
        Entity cam = Minecraft.getInstance().getCameraEntity();
        if (cam != null) {
            playerPos = SectionPos.of(cam.blockPosition());
        }
    }

    public static boolean shouldRender() {
        return SMACH.isEnabled();
    }

    public static List<OverlayData> buildChunk(OverlayDataProvider dataProvider, BlockPos sectionOrigin, ClientLevel level) {
        LevelChunk computationChunk = level.getChunkAt(sectionOrigin);

        ObjectArrayList<OverlayData> dataList = null;

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = sectionOrigin.offset(x, y, z);
                    var data = dataProvider.compute(level, computationChunk, pos, new Vec3i(x, y, z));
                    if (!data.valid()) {
                        continue;
                    }
                    if (dataList == null) {
                        dataList = new ObjectArrayList<>(300);
                    }
                    dataList.add(data);
                }
            }
        }

        return dataList;
    }

    public static void render(OverlayRenderer renderer, List<OverlayData> dataList, Function<ChunkSectionLayer, VertexConsumer> getOrCreateLayer, ClientLevel level, BlockAndTintGetter region, ModelBlockRenderer blockRenderer) {
        VertexConsumer consumer = getOrCreateLayer.apply(renderer.getOverlaySectionLayer());

        int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
        int lightMap = LightCoordsUtil.pack(overlayBrightness, overlayBrightness);

        for (var data : dataList) {
            renderer.build(
                    level,
                    data.pos(),
                    data,
                    consumer,
                    lightMap
            );
        }
    }

    private Compute() {}
}

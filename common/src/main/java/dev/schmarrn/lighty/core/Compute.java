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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.overlaystate.SMACH;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class Compute {
    /// Private copy of the player's currently occupied section.
    /// Used to clean up cachedBuffers that are out of range.
    /// Used to prioritize closer chunks when computing the overlay.
    /// Updated each tick.
    private static SectionPos playerPos = SectionPos.of(0,0,0);

    /// Cache of all computed GpuBuffers and so on.
    /// Gets used in LightyRenderer.
    private static final Object2ObjectOpenHashMap<SectionPos, BufferHolder> cachedBuffers = new Object2ObjectOpenHashMap<>();

    /// TreeSet used to create a priority hierarchy, while still
    /// avoiding duplicate entries.
    private static final TreeSet<SectionPos> toBeUpdated = new TreeSet<>(
            Comparator.comparingDouble(self -> {
                // As a distance measure, the manhattan distance is used,
                // additionally with a tie_breaker term based on the long-representation
                // of the section pos in question
                int manhattanDistance = self.distManhattan(playerPos);
                double tieBreaker = (double)self.asLong() / (double)Long.MAX_VALUE;
                return manhattanDistance + tieBreaker;
            })
    );

    static int computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);

    private static boolean outOfRange(SectionPos sPos) {
        return outOfRange(sPos, playerPos);
    }

    public static boolean outOfRange(SectionPos one, SectionPos two) {
        int absX = Math.abs(one.x() - two.x());
        int absZ = Math.abs(one.z() - two.z());

        return absX > computationDistance || absZ > computationDistance;
    }

    public static void clear() {
        toBeUpdated.clear();
        // Important to avoid a Memory leak!
        cachedBuffers.values().forEach(BufferHolder::close);

        cachedBuffers.clear();
        computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);
    }

    public static void updateBlockPos(BlockPos pos) {
        SectionPos sPos = SectionPos.of(pos);
        if (sPos.minBlockY() == pos.getY()) {
            // if we are on the y-border of a SubChunk, we need to update *both* SubChunks
            // see https://github.com/SchmarrnDevs/Lighty/issues/70
            updateSection(sPos.offset(0, -1, 0));
        }
        updateSection(sPos);
    }

    public static void updateSection(SectionPos sPos) {
        if (outOfRange(sPos) || !Minecraft.getInstance().levelRenderer.isSectionCompiledAndVisible(sPos.origin())) {
            return;
        }

        toBeUpdated.add(sPos);
    }

    private static BufferHolder buildChunk(OverlayRenderer renderer, List<OverlayDataProvider> dataProviders, SectionPos sPos, ClientLevel level, BufferHolder buffer) {
        BlockPos sectionOrigin = sPos.origin();
        LevelChunk computationChunk = level.getChunkAt(sectionOrigin);

        for (var dataProvider : dataProviders) {
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

            if (dataList == null) {
                buffer.invalidateBuffer(dataProvider.getIdentifier());
                continue;
            }

            BufferBuilder builder = Tesselator.getInstance().begin(renderer.getVertexFormatMode(), renderer.getVertexFormat());
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            for (var data : dataList) {
                renderer.build(level, data.pos(), data, builder, lightmap);
            }

            // builder.build() can return null if there wasn't any data added
            // in that case, the buffer automatically gets set as invalid
            buffer.upload(builder.build(), dataProvider.getIdentifier());
        }

        return buffer;
    }

    @Deprecated
    private static void queueNewChunksSlow(Level level, LevelRenderer levelRenderer) {
        queueNewChunksSlow(level, levelRenderer, null);
    }

    private static void queueNewChunksSlow(Level level, LevelRenderer levelRenderer, Frustum frustum) {
        for (int yy = level.getMinSectionY(); yy < level.getSectionsCount() + level.getMinSectionY(); ++yy) {
            for (int xx = -Compute.computationDistance; xx <= Compute.computationDistance; ++xx) {
                for (int zz = -Compute.computationDistance; zz <= Compute.computationDistance; ++zz) {
                    SectionPos chunkSection = SectionPos.of(playerPos.x() + xx, yy, playerPos.z() + zz);

                    var sectionOrigin = chunkSection.origin();
                    var chunkBoundaries = AABB.encapsulatingFullBlocks(sectionOrigin.offset(-1, -1, -1), sectionOrigin.offset(16, 16, 16));
                    if (frustum != null && !frustum.isVisible(chunkBoundaries)) {
                        continue;
                    }

                    if (!cachedBuffers.containsKey(chunkSection) && levelRenderer.isSectionCompiledAndVisible(chunkSection.origin())) {
                        toBeUpdated.add(chunkSection);
                    }
                }
            }
        }
    }

    public static Object2ObjectOpenHashMap<SectionPos, BufferHolder> computeCache(BlockPos cameraPos, Level level, LevelRenderer levelRenderer, Frustum frustum) {
        // update player position
        playerPos = SectionPos.of(cameraPos);

        if (!SMACH.isEnabled()) {
            return null;
        }

        queueNewChunksSlow(level, levelRenderer, frustum);

        // Get the currently active data providers and renderer
        List<OverlayDataProvider> dataProviders = DataProviderRegistry.getActiveProviders();
        OverlayRenderer renderer = RendererRegistry.getRenderer();

        // Remove any buffer that's outside the overlay distance
        for (var cacheIterator = Compute.cachedBuffers.object2ObjectEntrySet().fastIterator(); cacheIterator.hasNext();) {
            var entry = cacheIterator.next();
            if (outOfRange(entry.getKey())) {
                entry.getValue().close();
                cacheIterator.remove();
            }
        }

        // Compute at maximum as many chunks as specified
        for (int ii = level.getSectionsCount() * Config.CHUNKS_PER_TICK.getValue(); ii > 0;) {
            // get the next section pos
            SectionPos sectionPos = toBeUpdated.pollFirst();
            if (sectionPos == null) {
                // There is no more work to do, go home early! Feierabend :P
                break;
            }

            // If the section is out of range, do nothing and continue. This removes the section from toBeUpdated automatically.
            if (outOfRange(sectionPos)) {
                continue;
            }
            // If the section is in range, we compute the new buffers, reduce the counter!
            --ii;
            cachedBuffers.compute(
                    sectionPos,
                    (pos, bufferHolder) -> buildChunk(
                            renderer,
                            dataProviders,
                            pos,
                            (ClientLevel) level,
                            bufferHolder != null ? bufferHolder : new BufferHolder()
                    )
            );
        }
        return cachedBuffers;
    }


    private Compute() {}
}

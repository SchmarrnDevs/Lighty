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
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;

import java.util.*;

public class Compute {
    /// Private copy of the player's currently occupied section.
    /// Used to clean up cachedBuffers that are out of range.
    /// Used to prioritize closer chunks when computing the overlay.
    /// Updated each tick.
    private static SectionPos playerPos = SectionPos.of(0,0,0);

    /// Cache of all computed GpuBuffers and so on.
    /// Gets used in LightyRenderer.
    static final Map<SectionPos, BufferHolder> cachedBuffers = new HashMap<>();

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

    private static int computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);

    private static boolean outOfRange(SectionPos sPos) {
        int computationDistanceSquared = computationDistance * computationDistance;
        // squared X and Z
        int sqX = (sPos.x() - playerPos.x()) * (sPos.x() - playerPos.x());
        int sqZ =  (sPos.z() - playerPos.z()) * (sPos.z() - playerPos.z());

        return sqX > computationDistanceSquared || sqZ > computationDistanceSquared;
    }

    public static void clear() {
        toBeUpdated.clear();
        cachedBuffers.forEach((sectionPos, vertexBuffer) -> {
            // Important to avoid a Memory leak!
            vertexBuffer.close();
        });
        cachedBuffers.clear();
        computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);
    }

    public static void updateBlockPos(BlockPos pos) {
        SectionPos sPos = SectionPos.of(pos);
        if (sPos.minBlockY() == pos.getY()) {
            // if we are on the y-border of a SubChunk, we need to update *both* SubChunks
            // see https://github.com/SchmarrnDevs/Lighty/issues/70
            updateSubChunk(sPos.offset(0, -1, 0));
        }
        updateSubChunk(sPos);
    }

    public static void updateSubChunk(SectionPos sPos) {
        if (outOfRange(sPos)) {
            return;
        }

        toBeUpdated.add(sPos);
    }

    private static BufferHolder buildChunk(OverlayRenderer renderer, List<OverlayDataProvider> dataProviders, SectionPos sPos, ClientLevel level) {
        List<OverlayData> overlayData = new ArrayList<>();

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = sPos.origin().offset(x, y, z);

                    for (var dataProvider : dataProviders) {
                        var data = dataProvider.compute(level, pos, new Vec3i(x, y, z));
                        if (data.valid()) {
                            overlayData.add(data);
                        }
                    }
                }
            }
        }

        BufferHolder buffer = cachedBuffers.get(sPos);
        if (buffer == null) {
            buffer = new BufferHolder();
        }
        if (!overlayData.isEmpty()) {
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            for (var data : overlayData) {
                renderer.build(level, data.pos(), data, builder, lightmap);
            }

            buffer.upload(builder.buildOrThrow(), renderer.getChunkSectionLayer());
        }

        return buffer;
    }

    public static void computeCache(Minecraft client) {
        if (client.player == null || client.cameraEntity == null || client.level == null) {
            return;
        }

        // update state machine state that's based on items etc
        SMACH.updateCompute(client);

        // update player position
        playerPos = SectionPos.of(client.cameraEntity.blockPosition());

        if (!SMACH.isEnabled()) {
            return;
        }

        // Get the currently active data providers and renderer
        List<OverlayDataProvider> dataProviders = DataProviderRegistry.getActiveProviders();
        OverlayRenderer renderer = RendererRegistry.getRenderer();

        // Remove any buffer that's outside the overlay distance
        for (var it = cachedBuffers.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (outOfRange(entry.getKey())) {
                entry.getValue().close();
                it.remove();
            }
        }

        // Compute at maximum as many chunks as specified
        List<SectionPos> keepInUpdate = new ArrayList<>();
        for (int ii = client.level.getSectionsCount() * Config.CHUNKS_PER_TICK.getValue(); ii > 0;) {
            // get the next section pos
            SectionPos sectionPos = toBeUpdated.pollFirst();
            if (sectionPos == null) {
                // There is no more work to do, go home early! Feierabend :P
                break;
            }

            // as long as the section is in range...
            if (!outOfRange(sectionPos)) {
                // ... and the section is already compiled...
                if (!client.levelRenderer.isSectionCompiled(sectionPos.origin())) {
                    // chunk data isn't ready yet, keep in queue
                    keepInUpdate.add(sectionPos);
                    continue;
                }
                // ... we compute the new buffers, reduce the counter!
                --ii;
                cachedBuffers.compute(sectionPos, (pos, bufferHolder) -> {
                    if (bufferHolder != null) {
                        // Ensure to have a clean state after building
                        bufferHolder.close();
                    }
                    return buildChunk(renderer, dataProviders, pos, client.level);
                });
            }
        }

        toBeUpdated.addAll(keepInUpdate);
    }


    private Compute() {}
}

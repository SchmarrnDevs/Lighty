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

package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.DataProviders;
import dev.schmarrn.lighty.Renderers;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.compat.IrisCompat;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Compute {
    // Based on some observations, the hashset size only exceeds around 400 elements
    // when spectating through your world at max speed at an overlay distance of 2 chunks.
    // That's why I chose 550 as a reasonable default - not too big (a section pos is just 3 ints after all),
    // but it should be enough to avoid rehashing at a reasonable overlay distance
    private static final int INITIAL_HASHSET_CAPACITY = 550;
    private static HashSet<SectionPos> toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static HashSet<SectionPos> toBeRemoved = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static final Map<SectionPos, BufferHolder> cachedBuffers = new HashMap<>();
    private static ChunkPos playerPos = null;

    private static int computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);

    private static boolean outOfRange(SectionPos pos) {
        int computationDistanceSquared = computationDistance * computationDistance;
        if (playerPos == null) {
            return true;
        }
        // squared X and Z
        int sqX = (pos.x() - playerPos.x) * (pos.x() - playerPos.x);
        int sqZ =  (pos.z() - playerPos.z) * (pos.z() - playerPos.z);

        return sqX > computationDistanceSquared || sqZ > computationDistanceSquared;
    }

    public static void clear() {
        toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        cachedBuffers.forEach((sectionPos, vertexBuffer) -> {
            // Important to avoid a Memory leak!
            vertexBuffer.close();
        });
        cachedBuffers.clear();
        computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);
    }

    public static void updateBlockPos(BlockPos pos) {
        SectionPos spos = SectionPos.of(pos);
        if (spos.minBlockY() == pos.getY()) {
            // if we are on the y-border of a SubChunk, we need to update *both* SubChunks
            // see https://github.com/SchmarrnDevs/Lighty/issues/70
            updateSubChunk(spos.offset(0, -1, 0));
        }
        updateSubChunk(spos);
    }

    public static void updateSubChunk(SectionPos pos) {
        if (outOfRange(pos)) {
            return;
        }

        toBeUpdated.add(pos);
    }

    private static BufferHolder buildChunk(OverlayRenderer renderer, List<OverlayDataProvider> dataProviders, SectionPos chunkPos, ClientLevel world) {
        List<OverlayData> overlayData = new ArrayList<>();

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = chunkPos.origin().offset(x, y, z);

                    for (var dataProvider : dataProviders) {
                        var data = dataProvider.compute(world, pos, new Vec3i(x, y, z));
                        if (data.valid()) {
                            overlayData.add(data);
                        }
                    }
                }
            }
        }

        BufferHolder buffer = cachedBuffers.get(chunkPos);
        if (buffer == null) {
            buffer = new BufferHolder();
        }
        if (!overlayData.isEmpty()) {
            BufferBuilder builder = Tesselator.getInstance().begin(renderer.getRenderType().mode(), renderer.getRenderType().format());
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            for (var data : overlayData) {
                renderer.build(world, data.pos(), data, builder, lightmap);
            }

            buffer.upload(builder.buildOrThrow(), renderer.getRenderType());
        }

        return buffer;
    }

    public static void computeCache(Minecraft client) {
        if (client.player == null) return;

        // update state machine state that's based on items etc
        SMACH.updateCompute(client);

        if (!SMACH.isEnabled()) {
            return;
        }
        List<OverlayDataProvider> dataProviders = DataProviders.getActiveProviders();
        OverlayRenderer renderer = Renderers.getRenderer();

        ClientLevel world = client.level;

        if (client.player == null || world == null) {
            return;
        }

        cachedBuffers.forEach(((sectionPos, bufferHolder) -> {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            }
        }));

        HashSet<SectionPos> removeFromToBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        int sectionsComputeCounter = client.level.getSectionsCount() * Config.CHUNKS_PER_TICK.getValue();
        for (SectionPos sectionPos : toBeUpdated) {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            } else {
                if (sectionsComputeCounter <= 0 || !client.levelRenderer.isSectionCompiled(sectionPos.origin())) {
                    continue;
                }
                removeFromToBeUpdated.add(sectionPos);
                sectionsComputeCounter--;
                cachedBuffers.compute(sectionPos, (pos, vertexBuffer) -> {
                    if (vertexBuffer != null) {
                        // Ensure to have a clean state after building
                        vertexBuffer.close();
                    }
                    return buildChunk(renderer, dataProviders, pos, world);
                });
            }
        }

        // Don't know how to remove stuff while iterating over it in java, so this has to suffice.
        for (SectionPos pos : removeFromToBeUpdated) {
            toBeUpdated.remove(pos);
        }

        for (SectionPos sectionPos : toBeRemoved) {
            toBeUpdated.remove(sectionPos);
            var buf = cachedBuffers.remove(sectionPos);
            if (buf != null) {
                buf.close();
            }
        }

        toBeRemoved.clear();
    }

    public static void render(@Nullable Frustum frustum) {
        if (!SMACH.isEnabled()) return;

        if (frustum == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        ClientLevel world = minecraft.level;
        if (world == null) {
            return;
        }

        OverlayRenderer renderer = Renderers.getRenderer();
        RenderType renderType = renderer.getRenderType();

        GameRenderer gameRenderer = minecraft.gameRenderer;
        Camera camera = gameRenderer.getMainCamera();

        // update player position
        playerPos = new ChunkPos(camera.getBlockPosition());

        // fixes incompatible cached chunks when switching shaders on/off
        IrisCompat.fixIrisShaders();

        // save camera position to be able to later translate the different subsections
        Vec3 camPos = camera.getPosition();

        List<RenderPass.Draw> drawList = new ArrayList<>();
        // tracking the biggest *vertex* buffer, in case our data didn't return an *index* buffer as well
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        int biggestBufferSize = 0;
        RenderSystem.AutoStorageIndexBuffer asib = RenderSystem.getSequentialBuffer(renderType.mode());

        for (int x = -computationDistance + 1; x < computationDistance; ++x) {
            for (int z = -computationDistance + 1; z < computationDistance; ++z) {
                ChunkPos chunkPos = new ChunkPos(playerPos.x + x, playerPos.z + z);
                for (int i = 0; i < world.getSectionsCount(); ++i) {
                    var chunkSection = SectionPos.of(chunkPos, world.getMinSectionY() + i);
                    if (cachedBuffers.containsKey(chunkSection)) {
                        BufferHolder cachedBuffer = cachedBuffers.get(chunkSection);
                        // Only do the expensive frustum check if the buffer is valid
                        if (!cachedBuffer.isValid()) {
                            continue;
                        }
                        if (frustum.isVisible(AABB.encapsulatingFullBlocks(chunkSection.origin().offset(-1, -1, -1), chunkSection.origin().offset(16,16,16)))) {
                            Vec3 origin = new Vec3(chunkSection.origin());
                            Vec3 dPos = origin.subtract(camPos);

                            var gpuBuffers = cachedBuffer.getGpuBuffers();
                            for (var gpuBuffer : gpuBuffers) {
                                // If there is no index buffer
                                if (gpuBuffer.indexBuffer() == null) {
                                    // Try to reserve enough space to fit the vertex buffers
                                    if (gpuBuffer.indexCount() > biggestBufferSize) {
                                        biggestBufferSize = gpuBuffer.indexCount();
                                    }
                                }

                                drawList.add(new RenderPass.Draw(
                                        0, // slot (whatever a slot is in this context)
                                        gpuBuffer.vertexBuffer(),
                                        gpuBuffer.indexBuffer(),
                                        gpuBuffer.indexType(),
                                        0, // first index
                                        gpuBuffer.indexCount(),
                                        uniformUploader -> uniformUploader.upload("ModelOffset", (float)dPos.x(), (float)dPos.y(), (float)dPos.z())
                                ));
                            }
                        }
                    } else {
                        toBeUpdated.add(chunkSection);
                    }
                }
            }
        }

        GpuDevice device = RenderSystem.getDevice();
        GpuTexture tex = minecraft.getTextureManager().getTexture(renderer.getTextureLocation()).getTexture();
        // Highly inspired by RenderType#draw
        renderType.setupRenderState();

        RenderTarget renderTarget = renderType.getRenderTarget();

        // Index buffers/type for all the vertex data that didn't get its own IndexBuffer
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        GpuBuffer baseIndexBuffer = biggestBufferSize == 0 ? null : asib.getBuffer(biggestBufferSize);
        VertexFormat.IndexType baseIndexType = biggestBufferSize == 0 ? null : asib.type();

        try (RenderPass pass = device
                .createCommandEncoder()
                .createRenderPass(
                        renderTarget.getColorTexture(),
                        OptionalInt.empty(),
                        renderTarget.useDepth ? renderTarget.getDepthTexture() : null,
                        OptionalDouble.empty())) {

            pass.setPipeline(renderType.getRenderPipeline());

            pass.bindSampler("Sampler0", tex);
            for (int ii = 1; ii < 12; ++ii) {
                GpuTexture sampler = RenderSystem.getShaderTexture(ii);
                if (sampler != null) {
                    pass.bindSampler("Sampler" + ii, sampler);
                }
            }

            pass.drawMultipleIndexed(drawList, baseIndexBuffer, baseIndexType);
        }

        renderType.clearRenderState();
    }

    private Compute() {}
}

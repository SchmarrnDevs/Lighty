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
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class Compute {
    private static SectionPos playerPos= SectionPos.of(0,0,0);

    private static final Map<SectionPos, BufferHolder> cachedBuffers = new HashMap<>();
    private static final PriorityQueue<SectionPos> toBeUpdated = new PriorityQueue<>(
            Comparator.comparingInt(self -> self.distManhattan(playerPos))
    );
    private static final HashSet<SectionPos> workingOnIt = new HashSet<>();

    private static int computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get() + 1);

    private static boolean outOfRange(SectionPos pos) {
        int computationDistanceSquared = computationDistance * computationDistance;
        // squared X and Z
        int sqX = (pos.x() - playerPos.x()) * (pos.x() - playerPos.x());
        int sqZ =  (pos.z() - playerPos.z()) * (pos.z() - playerPos.z());

        return sqX > computationDistanceSquared || sqZ > computationDistanceSquared;
    }

    public static void clear() {
        toBeUpdated.clear();
        workingOnIt.clear();
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
        workingOnIt.add(pos);
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
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
            // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
            int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
            for (var data : overlayData) {
                renderer.build(world, data.pos(), data, builder, lightmap);
            }

            buffer.upload(builder.buildOrThrow(), renderer.getChunkSectionLayer());
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
            SectionPos sectionPos = toBeUpdated.poll();
            workingOnIt.remove(sectionPos);
            if (sectionPos == null) {
                // There is no more work to do, go home early!
                break;
            }

            if (!outOfRange(sectionPos)) {
                if (!client.levelRenderer.isSectionCompiled(sectionPos.origin())) {
                    // chunk data isn't ready yet, keep in queue
                    keepInUpdate.add(sectionPos);
                    continue;
                }
                // we do some work now, reduce the counter
                --ii;
                cachedBuffers.compute(sectionPos, (pos, bufferHolder) -> {
                    if (bufferHolder != null) {
                        // Ensure to have a clean state after building
                        bufferHolder.close();
                    }
                    return buildChunk(renderer, dataProviders, pos, world);
                });
            }
        }

        toBeUpdated.addAll(keepInUpdate);
        workingOnIt.addAll(keepInUpdate);
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

        GameRenderer gameRenderer = minecraft.gameRenderer;
        Camera camera = gameRenderer.getMainCamera();

        // update player position
        playerPos = SectionPos.of(camera.getBlockPosition());

        // fixes incompatible cached chunks when switching shaders on/off
        IrisCompat.fixIrisShaders();

        // save camera position to be able to later translate the different subsections
        Vec3 camPos = camera.getPosition();

        List<RenderPass.Draw<GpuBufferSlice[]>> drawList = new ArrayList<>();
        List<DynamicUniforms.Transform> transforms = new ArrayList<>();
        // tracking the biggest *vertex* buffer, in case our data didn't return an *index* buffer as well
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        int biggestBufferSize = 0;

        Vector4f unitColorModulator = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f textureMatrix = new Matrix4f();

        for (var section : minecraft.levelRenderer.getVisibleSections()) {
            SectionPos chunkSection = SectionPos.of(section.getRenderOrigin());
            if (cachedBuffers.containsKey(chunkSection)) {
                BufferHolder cachedBuffer = cachedBuffers.get(chunkSection);
                // Only continue if the buffer is valid
                if (!cachedBuffer.isValid()) {
                    continue;
                }
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

                    int currentTransformationIndex = transforms.size();
                    transforms.add(
                            new DynamicUniforms.Transform(
                                    RenderSystem.getModelViewMatrix(),
                                    unitColorModulator,
                                    new Vector3f((float)dPos.x(), (float)dPos.y(), (float)dPos.z()),
                                    textureMatrix,
                                    1.0F // Line Width
                            )
                    );

                    drawList.add(new RenderPass.Draw<>(
                            0, // slot (whatever a slot is in this context)
                            gpuBuffer.vertexBuffer(),
                            gpuBuffer.indexBuffer(),
                            gpuBuffer.indexType(),
                            0, // first index
                            gpuBuffer.indexCount(),
                            (bufferSlice, uniformUploader) -> uniformUploader.upload("DynamicTransforms", bufferSlice[currentTransformationIndex])
                    ));
                }
            } else {
                if (!workingOnIt.contains(chunkSection)) {
                    toBeUpdated.add(chunkSection);
                    workingOnIt.add(chunkSection);
                }
            }
        }

        GpuDevice device = RenderSystem.getDevice();
        GpuTextureView tex = minecraft.getTextureManager().getTexture(renderer.getTextureLocation()).getTextureView();

        ChunkSectionLayer chunkSectionLayer = renderer.getChunkSectionLayer();
        RenderTarget renderTarget = chunkSectionLayer.outputTarget();

        RenderSystem.AutoStorageIndexBuffer asib = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        // Index buffers/type for all the vertex data that didn't get its own IndexBuffer
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        GpuBuffer baseIndexBuffer = biggestBufferSize == 0 ? null : asib.getBuffer(biggestBufferSize);
        VertexFormat.IndexType baseIndexType = biggestBufferSize == 0 ? null : asib.type();

        GpuBufferSlice[] dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(transforms.toArray(new DynamicUniforms.Transform[0]));

        try (RenderPass pass = device
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Lighty Render Pass for" + chunkSectionLayer.label(),
                        renderTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTextureView(),
                        OptionalDouble.empty()
                )
        ) {
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindSampler("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView());

            if (chunkSectionLayer == ChunkSectionLayer.TRANSLUCENT) {
                drawList = drawList.reversed();
            }

            pass.setPipeline(chunkSectionLayer.pipeline());
            pass.bindSampler("Sampler0", tex);

            pass.drawMultipleIndexed(
                    drawList,
                    baseIndexBuffer,
                    baseIndexType,
                    List.of("DynamicTransforms"),
                    dynamicTransforms
            );
        }
    }

    private Compute() {}
}

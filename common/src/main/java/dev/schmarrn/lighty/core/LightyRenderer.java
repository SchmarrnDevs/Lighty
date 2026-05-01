package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.overlaystate.SMACH;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class LightyRenderer {
    private static final Vector4f UNIT_COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final Matrix4f DEFAULT_TEXTURE_MATRIX = new Matrix4f();

    /// See ChunkSectionsToRender
    public record DrawListData(
            List<RenderPass.Draw<GpuBufferSlice[]>> drawList,
            List<DynamicUniforms.ChunkSectionInfo> chunkSectionInfoList,
            List<DynamicUniforms.Transform> transformList
    ) {}

    private static int addChunkSectionInfo(SectionPos chunkSection, int width, int height, List<DynamicUniforms.ChunkSectionInfo> uniformList) {
        BlockPos origin = chunkSection.origin();
        Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());

        uniformList.add(
                new DynamicUniforms.ChunkSectionInfo(
                        modelViewMatrix,
                        origin.getX(),
                        origin.getY(),
                        origin.getZ(),
                        1.0f, // fade in, for now hard coded to 1
                        width,
                        height
                )
        );
        // return index of newly added uniform
        return uniformList.size() - 1;
    }

    private static int addTransform(SectionPos chunkSection, Vec3 camPos, List<DynamicUniforms.Transform> uniformList) {
        Vec3 origin = new Vec3(chunkSection.origin());
        Vec3 dPos = origin.subtract(camPos);

        Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
        modelViewMatrix.translate((float) dPos.x, (float) dPos.y, (float) dPos.z);

        uniformList.add(
                new DynamicUniforms.Transform(
                        modelViewMatrix,
                        UNIT_COLOR_MODULATOR,
                        new Vector3f(),
                        DEFAULT_TEXTURE_MATRIX
                )
        );
        // return index of newly added uniform
        return uniformList.size() - 1;
    }

    private static int addData(LightyExtractor.RenderData gpuBuffer,
                               int biggestBufferSize,
                               List<RenderPass.Draw<GpuBufferSlice[]>> drawList,
                               String uniformName,
                               int currentIndex) {
        // Prepare the render data
        // If there is no index buffer available...
        if (gpuBuffer.indexBuffer() == null) {
            // ... try to reserve enough space to fit the vertex buffers
            if (gpuBuffer.indexCount() > biggestBufferSize) {
                biggestBufferSize = gpuBuffer.indexCount();
            }
        }

        drawList.add(new RenderPass.Draw<>(
                0, // slot (whatever a slot is in this context)
                gpuBuffer.vertexBuffer(),
                gpuBuffer.indexBuffer(),
                gpuBuffer.indexType(),
                0, // first index
                gpuBuffer.indexCount(),
                0,
                (bufferSlice, uniformUploader) -> {
                    uniformUploader.upload(uniformName, bufferSlice[currentIndex]);
                }
        ));
        return biggestBufferSize;
    }

    private interface DoTheThing {
        int theThing(LightyExtractor.RenderData buffers, int biggestBufferSize, SectionPos chunkSection);
    }

    private static int goThroughEachBuffer(Object2ObjectOpenHashMap<SectionPos, LightyExtractor.RenderData> cache, DoTheThing fn) {
        int biggestBufferSize = 0;
        for (var cacheIterator = cache.object2ObjectEntrySet().fastIterator(); cacheIterator.hasNext();) {
            var entry = cacheIterator.next();
            SectionPos chunkSection = entry.getKey();
            LightyExtractor.RenderData cachedBuffer = entry.getValue();
            // Only continue if the buffer is valid
            biggestBufferSize = fn.theThing(cachedBuffer, biggestBufferSize, chunkSection);
        }
        return biggestBufferSize;
    }

    private static String getUniformName(OverlaySectionLayer layer) {
        if (layer.pipeline() == LightyPipelines.TERRAIN_TRANSLUCENT || layer.pipeline() == LightyPipelines.TERRAIN_CUTOUT) {
            return "ChunkSection";
        } else if (layer.pipeline() == LightyPipelines.LINES) {
            return "DynamicTransforms";
        } else {
            Lighty.LOGGER.error("LightyRenderer: getUniformName: Don't how to handle RenderPipeline {} of mode {}.", layer.pipeline(), layer.label());
            throw new RuntimeException("Lighty unknown RenderPipeline");
        }
    }

    public static int prepareData(DrawListData drawList, SectionPos sectionPos, LightyExtractor.RenderData cache, int previousBiggestBufferSize, Vec3 camPos, GpuTextureView tex, OverlaySectionLayer layer){
        String uniformName = getUniformName(layer);
        int biggestBufferSize = previousBiggestBufferSize;
        if (uniformName.equals("ChunkSection")) {
            List<DynamicUniforms.ChunkSectionInfo> chunkSectionInfoList = new ArrayList<>();
            biggestBufferSize = addData(
                cache,
                biggestBufferSize,
                drawList.drawList,
                "ChunkSection",
                addChunkSectionInfo(sectionPos, tex.getWidth(0), tex.getHeight(0), chunkSectionInfoList)
            );
            drawList.chunkSectionInfoList.addAll(chunkSectionInfoList);
        } else if (uniformName.equals("DynamicTransforms")) {
            List<DynamicUniforms.Transform> transformList = new ArrayList<>();
            biggestBufferSize = addData(
                    cache,
                    biggestBufferSize,
                    drawList.drawList,
                    "DynamicTransforms",
                    addTransform(sectionPos, camPos, transformList)
            );
            drawList.transformList.addAll(transformList);
        } else {
            throw new IllegalStateException("unknown uniform type");
        }
        return biggestBufferSize;
    }

    public static void render(Vec3 camPos, Object2ObjectOpenHashMap<SectionPos, Map<OverlaySectionLayer, LightyExtractor.RenderData>> cache) {
        if (!SMACH.isEnabled()) {
            return;
        }

        // Get required data
        Minecraft minecraft = Minecraft.getInstance();
        OverlayRenderer renderer = RendererRegistry.getRenderer();

        // Do the rendering
        GpuDevice device = RenderSystem.getDevice();
        // Get the texture specified in the overlay
        GpuTextureView tex = minecraft.getTextureManager().getTexture(renderer.getTextureLocation()).getTextureView();

        // Create the drawlists
        Map<OverlaySectionLayer, DrawListData> drawListData = new EnumMap<>(OverlaySectionLayer.class);
        Map<OverlaySectionLayer, Integer> biggestIndex = new EnumMap<>(OverlaySectionLayer.class);
        for (var cacheIterator = cache.object2ObjectEntrySet().fastIterator(); cacheIterator.hasNext();) {
            var entry = cacheIterator.next();
            var sectionPos = entry.getKey();

            entry.getValue().forEach((layer, renderData) -> {
                DrawListData data = drawListData.get(layer);
                if (data == null) {
                    data = new DrawListData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                }

                int newBiggestIndex = prepareData(data, sectionPos, renderData, biggestIndex.getOrDefault(layer, 0), camPos, tex, layer);
                biggestIndex.put(layer, newBiggestIndex);
                drawListData.put(layer, data);
            });
        }

        for (var layer : OverlaySectionLayer.values()) {
            // See ChunkSectionLayerGroup:outputTarget
            RenderTarget renderTarget = layer.pipeline() == LightyPipelines.TERRAIN_TRANSLUCENT ? minecraft.levelRenderer.getTranslucentTarget() : minecraft.getMainRenderTarget();
            if (renderTarget == null) {
                renderTarget = minecraft.getMainRenderTarget();
            }

            DrawListData data = drawListData.get(layer);
            if (data == null) continue;

            GpuBufferSlice[] uniforms = null;
            if (getUniformName(layer).equals("ChunkSection")) {
                uniforms = RenderSystem.getDynamicUniforms().writeChunkSections(data.chunkSectionInfoList.toArray(new DynamicUniforms.ChunkSectionInfo[0]));
            } else if (getUniformName(layer).equals("DynamicTransforms")) {
                uniforms = RenderSystem.getDynamicUniforms().writeTransforms(data.transformList.toArray(new DynamicUniforms.Transform[0]));

            }

            RenderSystem.AutoStorageIndexBuffer asib = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            // Index buffers/type for all the vertex data that didn't get its own IndexBuffer
            // See LevelRenderer#renderSectionLayer for the place of inspiration
            GpuBuffer baseIndexBuffer = biggestIndex.getOrDefault(layer, 0) == 0 ? null : asib.getBuffer(biggestIndex.getOrDefault(layer, 0));
            VertexFormat.IndexType baseIndexType = biggestIndex.getOrDefault(layer, 0) == 0 ? null : asib.type();

            try (RenderPass pass = device
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Lighty Render Pass for" + layer.pipeline().getLocation(),
                            renderTarget.getColorTextureView(),
                            OptionalInt.empty(),
                            renderTarget.getDepthTextureView(),
                            OptionalDouble.empty()
                    )
            ) {
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", tex, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                pass.bindTexture("Sampler2", minecraft.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

                pass.setPipeline(layer.pipeline());

                pass.drawMultipleIndexed(
                        layer.pipeline() == LightyPipelines.TERRAIN_TRANSLUCENT ? data.drawList.reversed() : data.drawList,
                        baseIndexBuffer,
                        baseIndexType,
                        List.of(getUniformName(layer)),
                        uniforms
                );

            }
        }
    }
}

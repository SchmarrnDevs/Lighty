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
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class LightyRenderer {
    private static final Vector4f UNIT_COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final Matrix4f DEFAULT_TEXTURE_MATRIX = new Matrix4f();

    /// See ChunkSectionsToRender
    // TODO? Maybe integrate Lighty render code more tightly into Minecraft render code, but wait if there are major changes in next versions until I do so
    public record DrawListData(
            List<RenderPass.Draw<GpuBufferSlice[]>> drawList,
            int maxIndicesRequired,
            GpuBufferSlice[] uniforms,
            boolean isChunkSectionUniform
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

    private static int addData(SectionBuffers gpuBuffer,
                              int biggestBufferSize,
                              List<RenderPass.Draw<GpuBufferSlice[]>> drawList,
                              String uniformName,
                              int currentIndex) {
        // Prepare the render data
        // If there is no index buffer available...
        if (gpuBuffer.getIndexBuffer() == null) {
            // ... try to reserve enough space to fit the vertex buffers
            if (gpuBuffer.getIndexCount() > biggestBufferSize) {
                biggestBufferSize = gpuBuffer.getIndexCount();
            }
        }

        drawList.add(new RenderPass.Draw<>(
                0, // slot (whatever a slot is in this context)
                gpuBuffer.getVertexBuffer(),
                gpuBuffer.getIndexBuffer(),
                gpuBuffer.getIndexType(),
                0, // first index
                gpuBuffer.getIndexCount(),
                (bufferSlice, uniformUploader) -> {
                    uniformUploader.upload(uniformName, bufferSlice[currentIndex]);
                }
        ));
        return biggestBufferSize;
    }

    private interface DoTheThing {
        int theThing(SectionBuffers buffers, int biggestBufferSize, SectionPos chunkSection);
    }

    private static int goThroughEachBuffer(Object2ObjectOpenHashMap<SectionPos, BufferHolder> cache, DoTheThing fn) {
        int biggestBufferSize = 0;
        for (var cacheIterator = cache.object2ObjectEntrySet().fastIterator(); cacheIterator.hasNext();) {
            var entry = cacheIterator.next();
            SectionPos chunkSection = entry.getKey();
            BufferHolder cachedBuffer = entry.getValue();
            var gpuBuffers = cachedBuffer.getGpuBuffers();
            for (var bufferIterator = gpuBuffers.object2ObjectEntrySet().fastIterator(); bufferIterator.hasNext();) {
                var bufferEntry = bufferIterator.next();
                if (!cachedBuffer.isValid(bufferEntry.getKey())) {
                    continue;
                }

                // Only continue if the buffer is valid
                biggestBufferSize = fn.theThing(bufferEntry.getValue(), biggestBufferSize, chunkSection);
            }
        }
        return biggestBufferSize;
    }

    private static String getUniformName(OverlayRenderer renderer) {
        if (renderer.getPipeline() == LightyPipelines.TERRAIN_TRANSLUCENT || renderer.getPipeline() == LightyPipelines.TERRAIN_CUTOUT) {
            return "ChunkSection";
        } else if (renderer.getPipeline() == LightyPipelines.LINES) {
            return "DynamicTransforms";
        } else {
            Lighty.LOGGER.error("LightyRenderer: getUniformName: Don't how to handle RenderPipeline {} of mode {}.", renderer.getPipeline(), renderer.getIdentifier().toString());
            throw new RuntimeException("Lighty unknown RenderPipeline");
        }
    }

    public static DrawListData prepareData(Object2ObjectOpenHashMap<SectionPos, BufferHolder> cache, Vec3 camPos, GpuTextureView tex, OverlayRenderer renderer){
        // Create required rendering lists
        List<RenderPass.Draw<GpuBufferSlice[]>> drawList = new ArrayList<>();

        // tracking the biggest *vertex* buffer, in case our data didn't return an *index* buffer as well
        // See LevelRenderer#renderSectionLayer (1.21.5) for the place of inspiration
        int biggestBufferSize;
        GpuBufferSlice[] dynamicTransforms;
        String uniformName = getUniformName(renderer);
        if (uniformName.equals("ChunkSection")) {
            List<DynamicUniforms.ChunkSectionInfo> chunkSectionInfoList = new ArrayList<>();
            biggestBufferSize = goThroughEachBuffer(cache,
                    (buffers, previousBiggestBufferSize, chunkSection) -> addData(
                            buffers,
                            previousBiggestBufferSize,
                            drawList,
                            "ChunkSection",
                            addChunkSectionInfo(chunkSection, tex.getWidth(0), tex.getHeight(0), chunkSectionInfoList)
                    )
            );
            dynamicTransforms = RenderSystem.getDynamicUniforms().writeChunkSections(chunkSectionInfoList.toArray(new DynamicUniforms.ChunkSectionInfo[0]));
        } else if (uniformName.equals("DynamicTransforms")) {
            List<DynamicUniforms.Transform> transformList = new ArrayList<>();
            biggestBufferSize = goThroughEachBuffer(cache,
                    (buffers, previousBiggestBufferSize, chunkSection) -> addData(
                            buffers,
                            previousBiggestBufferSize,
                            drawList,
                            "DynamicTransforms",
                            addTransform(chunkSection, camPos, transformList)
                    )
            );
            dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(transformList.toArray(new DynamicUniforms.Transform[0]));
        } else {
            throw new IllegalStateException("unknown uniform type");
        }

        return new DrawListData(drawList, biggestBufferSize, dynamicTransforms, true);
    }

    public static void render(Vec3 camPos, Object2ObjectOpenHashMap<SectionPos, BufferHolder> cache) {
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

        DrawListData data = prepareData(cache, camPos, tex, renderer);

        RenderPipeline pipeline = renderer.getPipeline();
        // See ChunkSectionLayerGroup:outputTarget
        RenderTarget renderTarget = renderer.getPipeline() == LightyPipelines.TERRAIN_TRANSLUCENT ? minecraft.levelRenderer.getTranslucentTarget() : minecraft.getMainRenderTarget();
        if (renderTarget == null) {
            renderTarget = minecraft.getMainRenderTarget();
        }

        RenderSystem.AutoStorageIndexBuffer asib = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        // Index buffers/type for all the vertex data that didn't get its own IndexBuffer
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        GpuBuffer baseIndexBuffer = data.maxIndicesRequired == 0 ? null : asib.getBuffer(data.maxIndicesRequired);
        VertexFormat.IndexType baseIndexType = data.maxIndicesRequired == 0 ? null : asib.type();

        try (RenderPass pass = device
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Lighty Render Pass for" + pipeline.getLocation(),
                        renderTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTextureView(),
                        OptionalDouble.empty()
                )
        ) {
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

            pass.setPipeline(pipeline);
            pass.bindTexture("Sampler0", tex, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));

            pass.drawMultipleIndexed(
                    pipeline == LightyPipelines.TERRAIN_TRANSLUCENT ? data.drawList.reversed() : data.drawList,
                    baseIndexBuffer,
                    baseIndexType,
                    List.of(getUniformName(renderer)),
                    data.uniforms
            );
        }
    }
}

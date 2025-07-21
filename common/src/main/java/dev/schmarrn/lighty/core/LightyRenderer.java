package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.compat.IrisCompat;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
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
    private record Data(List<RenderPass.Draw<GpuBufferSlice[]>> drawList, int maxIndicesRequired, GpuBufferSlice[] dynamicTransforms) {}

    private static int addData(SectionPos chunkSection, SectionBuffers gpuBuffer, Vec3 camPos, int biggestBufferSize, List<RenderPass.Draw<GpuBufferSlice[]>> drawList, List<DynamicUniforms.Transform> transforms) {
        // Calculate the translation required to place the section at its right place
        Vec3 origin = new Vec3(chunkSection.origin());
        Vec3 dPos = origin.subtract(camPos);

        // Prepare the render data
        // If there is no index buffer available...
        if (gpuBuffer.getIndexBuffer() == null) {
            // ... try to reserve enough space to fit the vertex buffers
            if (gpuBuffer.getIndexCount() > biggestBufferSize) {
                biggestBufferSize = gpuBuffer.getIndexCount();
            }
        }

        // Get index of the current transform,
        // which is the size of the list *before* adding the transform to the list
        int currentTransformationIndex = transforms.size();
        transforms.add(
                new DynamicUniforms.Transform(
                        RenderSystem.getModelViewMatrix(),
                        UNIT_COLOR_MODULATOR,
                        new Vector3f((float)dPos.x(), (float)dPos.y(), (float)dPos.z()),
                        DEFAULT_TEXTURE_MATRIX,
                        1.0F // Line Width
                )
        );

        drawList.add(new RenderPass.Draw<>(
                0, // slot (whatever a slot is in this context)
                gpuBuffer.getVertexBuffer(),
                gpuBuffer.getIndexBuffer(),
                gpuBuffer.getIndexType(),
                0, // first index
                gpuBuffer.getIndexCount(),
                (bufferSlice, uniformUploader) -> uniformUploader.upload("DynamicTransforms", bufferSlice[currentTransformationIndex])
        ));
        return biggestBufferSize;
    }

    private static int goThroughEachBuffer(Minecraft minecraft, Camera camera, Vec3 camPos, Frustum frustum, List<RenderPass.Draw<GpuBufferSlice[]>> drawList, List<DynamicUniforms.Transform> transforms) {
        int biggestBufferSize = 0;
        for (var entry : Compute.cachedBuffers.entrySet()) {
            SectionPos chunkSection = entry.getKey();
            BufferHolder cachedBuffer = entry.getValue();

            for (var bufferEntry : cachedBuffer.getGpuBuffers().entrySet()) {
                String key = bufferEntry.getKey();
                if (!cachedBuffer.isValid(key)) {
                    continue;
                }
                if (!frustum.isVisible(
                        AABB.encapsulatingFullBlocks(chunkSection.origin().offset(-1, -1, -1), chunkSection.origin().offset(16, 16, 16))
                )) {
                    continue;
                }
                // Only continue if the buffer is valid
                biggestBufferSize = addData(chunkSection, bufferEntry.getValue(), camPos, biggestBufferSize, drawList, transforms);
            }
        }
        return biggestBufferSize;
    }

    private static int goThroughEachSection(Minecraft minecraft, Camera camera, Vec3 camPos, Frustum frustum, List<RenderPass.Draw<GpuBufferSlice[]>> drawList, List<DynamicUniforms.Transform> transforms) {
        ChunkPos cameraChunkPos = new ChunkPos(camera.getBlockPosition());
        int biggestBufferSize = 0;

        for (int xx = -Compute.computationDistance + 1; xx < Compute.computationDistance; ++xx) {
            for (int zz = -Compute.computationDistance + 1; zz < Compute.computationDistance; ++zz) {
                ChunkPos chunkPos = new ChunkPos(cameraChunkPos.x + xx, cameraChunkPos.z + zz);

                for (int ii = 0; ii < minecraft.level.getSectionsCount(); ++ii) {
                    SectionPos chunkSection = SectionPos.of(chunkPos, ii + minecraft.level.getMinSectionY());
                    if (!minecraft.levelRenderer.isSectionCompiled(chunkSection.origin())) {
                        // Don't bother doing anything if the chunk isn't rendered yet
                        continue;
                    }

                    if (Compute.cachedBuffers.containsKey(chunkSection)) {
                        BufferHolder cachedBuffer = Compute.cachedBuffers.get(chunkSection);
                        for (var entry : cachedBuffer.getGpuBuffers().entrySet()) {
                            String key = entry.getKey();
                            if (!cachedBuffer.isValid(key)) {
                                continue;
                            }
                            if (!frustum.isVisible(
                                    AABB.encapsulatingFullBlocks(chunkSection.origin().offset(-1, -1, -1), chunkSection.origin().offset(16, 16, 16))
                            )) {
                                continue;
                            }
                            // Only continue if the buffer is valid
                            biggestBufferSize = addData(chunkSection, entry.getValue(), camPos, biggestBufferSize, drawList, transforms);
                        }
                    }
                }
            }
        }
        return biggestBufferSize;
    }

    private static int goThroughVisibleSections(Minecraft minecraft, Camera camera, Vec3 camPos, Frustum frustum, List<RenderPass.Draw<GpuBufferSlice[]>> drawList, List<DynamicUniforms.Transform> transforms) {
        int biggestBufferSize = 0;
        for (var sections : minecraft.levelRenderer.getVisibleSections()) {
            var chunkSection = SectionPos.of(sections.getRenderOrigin());
            if (Compute.cachedBuffers.containsKey(chunkSection)) {
                BufferHolder cachedBuffer = Compute.cachedBuffers.get(chunkSection);
                for (var entry : cachedBuffer.getGpuBuffers().entrySet()) {
                    String key = entry.getKey();
                    if (!cachedBuffer.isValid(key)) {
                        continue;
                    }
                    // Only continue if the buffer is valid
                    biggestBufferSize = addData(chunkSection, entry.getValue(), camPos, biggestBufferSize, drawList, transforms);
                }
            }
        }
        return biggestBufferSize;
    }

    private static Data prepareData(Minecraft minecraft, Frustum frustum){
        // Get some basic stuff
        Camera camera = minecraft.gameRenderer.getMainCamera();

        // fixes incompatible cached chunks when switching shaders on/off
        IrisCompat.INSTANCE.fixIrisShaders();

        // save camera position to be able to later translate the different sections
        Vec3 camPos = camera.getPosition();

        // Create required rendering lists
        List<RenderPass.Draw<GpuBufferSlice[]>> drawList = new ArrayList<>();
        List<DynamicUniforms.Transform> transforms = new ArrayList<>();

        // tracking the biggest *vertex* buffer, in case our data didn't return an *index* buffer as well
        // See LevelRenderer#renderSectionLayer (1.21.5) for the place of inspiration
        int biggestBufferSize = goThroughEachSection(minecraft, camera, camPos, frustum, drawList, transforms);

        GpuBufferSlice[] dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(transforms.toArray(new DynamicUniforms.Transform[0]));
        return new Data(drawList, biggestBufferSize, dynamicTransforms);
    }

    public static void render(Frustum frustum) {
        if (!SMACH.isEnabled()) return;

        // Get required data
        Minecraft minecraft = Minecraft.getInstance();
        OverlayRenderer renderer = RendererRegistry.getRenderer();
        Data data = prepareData(minecraft, frustum);

        // Do the rendering
        GpuDevice device = RenderSystem.getDevice();
        // Get the texture specified in the overlay
        GpuTextureView tex = minecraft.getTextureManager().getTexture(renderer.getTextureLocation()).getTextureView();

        RenderPipeline pipeline = renderer.getPipeline();
        RenderTarget renderTarget = minecraft.getMainRenderTarget();

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
            pass.bindSampler("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView());

            pass.setPipeline(pipeline);
            pass.bindSampler("Sampler0", tex);

            pass.drawMultipleIndexed(
                    pipeline == LightyPipelines.TERRAIN_TRANSLUCENT ? data.drawList.reversed() : data.drawList,
                    baseIndexBuffer,
                    baseIndexType,
                    List.of("DynamicTransforms"),
                    data.dynamicTransforms
            );
        }
    }
}

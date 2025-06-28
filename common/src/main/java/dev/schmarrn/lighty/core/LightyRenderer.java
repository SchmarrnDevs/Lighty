package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
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
    private record Data(List<RenderPass.Draw<GpuBufferSlice[]>> drawList, int maxIndicesRequired, GpuBufferSlice[] dynamicTransforms) {}

    private static Data prepareData(Minecraft minecraft){
        // Get some basic stuff
        Camera camera = minecraft.gameRenderer.getMainCamera();

        // fixes incompatible cached chunks when switching shaders on/off
        IrisCompat.fixIrisShaders();

        // save camera position to be able to later translate the different sections
        Vec3 camPos = camera.getPosition();

        // Create required rendering lists
        List<RenderPass.Draw<GpuBufferSlice[]>> drawList = new ArrayList<>();
        List<DynamicUniforms.Transform> transforms = new ArrayList<>();

        // tracking the biggest *vertex* buffer, in case our data didn't return an *index* buffer as well
        // See LevelRenderer#renderSectionLayer (1.21.5) for the place of inspiration
        int biggestBufferSize = 0;

        for (var section : minecraft.levelRenderer.getVisibleSections()) {
            SectionPos chunkSection = SectionPos.of(section.getRenderOrigin());

            if (Compute.cachedBuffers.containsKey(chunkSection)) {
                BufferHolder cachedBuffer = Compute.cachedBuffers.get(chunkSection);
                // Only continue if the buffer is valid
                if (!cachedBuffer.isValid()) {
                    continue;
                }
                // Calculate the translation required to place the section at its right place
                Vec3 origin = new Vec3(chunkSection.origin());
                Vec3 dPos = origin.subtract(camPos);

                // Prepare the render data
                var gpuBuffers = cachedBuffer.getGpuBuffers();
                for (var gpuBuffer : gpuBuffers) {
                    // If there is no index buffer available...
                    if (gpuBuffer.indexBuffer() == null) {
                        // ... try to reserve enough space to fit the vertex buffers
                        if (gpuBuffer.indexCount() > biggestBufferSize) {
                            biggestBufferSize = gpuBuffer.indexCount();
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
                            gpuBuffer.vertexBuffer(),
                            gpuBuffer.indexBuffer(),
                            gpuBuffer.indexType(),
                            0, // first index
                            gpuBuffer.indexCount(),
                            (bufferSlice, uniformUploader) -> uniformUploader.upload("DynamicTransforms", bufferSlice[currentTransformationIndex])
                    ));
                }
            } else {
                // Chunk data doesn't exist in our cache, queue computation
                Compute.updateSubChunk(chunkSection);
            }
        }
        GpuBufferSlice[] dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(transforms.toArray(new DynamicUniforms.Transform[0]));
        return new Data(drawList, biggestBufferSize, dynamicTransforms);
    }

    public static void render() {
        if (!SMACH.isEnabled()) return;

        // Get required data
        Minecraft minecraft = Minecraft.getInstance();
        OverlayRenderer renderer = RendererRegistry.getRenderer();
        Data data = prepareData(minecraft);

        // Do the rendering
        GpuDevice device = RenderSystem.getDevice();
        // Get the texture specified in the overlay
        GpuTextureView tex = minecraft.getTextureManager().getTexture(renderer.getTextureLocation()).getTextureView();

        ChunkSectionLayer chunkSectionLayer = renderer.getChunkSectionLayer();
        RenderTarget renderTarget = chunkSectionLayer.outputTarget();

        RenderSystem.AutoStorageIndexBuffer asib = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        // Index buffers/type for all the vertex data that didn't get its own IndexBuffer
        // See LevelRenderer#renderSectionLayer for the place of inspiration
        GpuBuffer baseIndexBuffer = data.maxIndicesRequired == 0 ? null : asib.getBuffer(data.maxIndicesRequired);
        VertexFormat.IndexType baseIndexType = data.maxIndicesRequired == 0 ? null : asib.type();


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

            pass.setPipeline(chunkSectionLayer.pipeline());
            pass.bindSampler("Sampler0", tex);

            pass.drawMultipleIndexed(
                    chunkSectionLayer == ChunkSectionLayer.TRANSLUCENT ? data.drawList.reversed() : data.drawList,
                    baseIndexBuffer,
                    baseIndexType,
                    List.of("DynamicTransforms"),
                    data.dynamicTransforms
            );
        }
    }
}

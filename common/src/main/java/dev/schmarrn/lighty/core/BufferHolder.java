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

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import dev.schmarrn.lighty.Lighty;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;

public class BufferHolder implements AutoCloseable {
    // List because we can hold multiple gpuBuffers from different data providers
    private final Object2ObjectOpenHashMap<Identifier, SectionBuffers> overlayBuffers = new Object2ObjectOpenHashMap<>();
    private final Object2BooleanMap<Identifier> isValid = new Object2BooleanOpenHashMap<>();

    private static final int BUFFER_TYPE_VERTEX = 40;
    private static final int BUFFER_TYPE_INDEX = 72;

    public boolean isValid(Identifier key) {
        return this.isValid.getOrDefault(key, false);
    }

    public void invalidateBuffer(Identifier key) {
        this.isValid.put(key, false);
    }

    @Override
    public void close() {
        this.overlayBuffers.values().forEach(SectionBuffers::close);
        this.overlayBuffers.clear();
    }

    void upload(MeshData data, Identifier dataProviderKey) {
        if (data == null) {
            this.isValid.put(dataProviderKey, false);
            return;
        }
        // See CompiledSectionMesh::uploadMeshLayer (1.21.6) for place of inspiration
        SectionBuffers previous = this.overlayBuffers.get(dataProviderKey);
        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder commandEncoder = device.createCommandEncoder();

        if (previous != null) {
            // If there already are buffers, we'll try to re-use them
            if (previous.getVertexBuffer().size() < data.vertexBuffer().remaining()) {
                // If the previous buffer is too small, we delete it and create a new, bigger one
                previous.getVertexBuffer().close();
                previous.setVertexBuffer(
                        device.createBuffer(
                                () -> "Lighty vertex buffer for " + dataProviderKey,
                                BUFFER_TYPE_VERTEX,
                                data.vertexBuffer()
                        )
                );
            } else if (!previous.getVertexBuffer().isClosed()) {
                // Else, if we can still write to the vertex buffer (at least I think that's what this means)
                commandEncoder.writeToBuffer(previous.getVertexBuffer().slice(), data.vertexBuffer());
            } else {
                // Interestingly, Minecraft code doesn't contain any else here, so dunno when both statements would fail,
                // or if they'd even fail.
                // I'll put a log statement here, just for funsies
                Lighty.LOGGER.info("I am curious whether this will ever trigger (BufferHolder::upload)");
            }

            ByteBuffer indexBuffer = data.indexBuffer();
            if (indexBuffer != null) {
                if (previous.getIndexBuffer() != null && previous.getIndexBuffer().size() >= indexBuffer.remaining()) {
                    if (!previous.getIndexBuffer().isClosed()) {
                        commandEncoder.writeToBuffer(previous.getIndexBuffer().slice(), indexBuffer);
                    }
                } else {
                    if (previous.getIndexBuffer() != null) {
                        previous.getIndexBuffer().close();
                    }

                    previous.setIndexBuffer(
                            device.createBuffer(
                                    () -> "Lighty index buffer for" + dataProviderKey,
                                    BUFFER_TYPE_INDEX,
                                    indexBuffer
                            )
                    );
                }
            } else if (previous.getIndexBuffer() != null) {
                // If the previous index buffer isn't null, but the current one is, delete the previous one
                previous.getIndexBuffer().close();
                previous.setIndexBuffer(null);
            }

            previous.setIndexCount(data.drawState().indexCount());
            previous.setIndexType(indexBuffer != null ? data.drawState().indexType() : null);
        } else {
            // there are no previous buffers, create new ones
            GpuBuffer vertexBuffer = device.createBuffer(
                    () -> "Lighty vertex buffer for " + dataProviderKey,
                    BUFFER_TYPE_VERTEX,
                    data.vertexBuffer()
            );

            ByteBuffer indexBuffer = data.indexBuffer();
            GpuBuffer gpuIndexBuffer = indexBuffer != null
                    ? device.createBuffer(
                            () -> "Lighty index buffer for" + dataProviderKey,
                            BUFFER_TYPE_INDEX,
                            indexBuffer
                    ) : null;

            this.overlayBuffers.put(dataProviderKey, new SectionBuffers(
                    vertexBuffer,
                    gpuIndexBuffer,
                    data.drawState().indexCount(),
                    gpuIndexBuffer != null ? data.drawState().indexType() : null
            ));
        }

        data.close();
        this.isValid.put(dataProviderKey, true);
    }

    public Object2ObjectOpenHashMap<Identifier, SectionBuffers> getGpuBuffers() {
        return this.overlayBuffers;
    }
}

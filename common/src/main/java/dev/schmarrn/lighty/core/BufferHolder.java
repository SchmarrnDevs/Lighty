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
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.util.ArrayList;
import java.util.List;

public class BufferHolder {
    public record Data(GpuBuffer vertexBuffer, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, int indexCount) {}
    // List because we can hold multiple gpuBuffers from different data providers
    private final List<Data> gpuBuffers;

    private final ByteBufferBuilder sharedBuffer;

    private static final int BUFFER_TYPE_VERTEX = 40;
    private static final int BUFFER_TYPE_INDEX = 72;


    BufferHolder() {
        gpuBuffers = new ArrayList<>();
        // Got magic number from OutlineBufferSource:14
        // Maybe better magic number at RenderBuffers:38 (786432)
        sharedBuffer = new ByteBufferBuilder(1536);
    }

    boolean isValid() {
        return !gpuBuffers.isEmpty();
    }

    void close() {
        for (var buffer : gpuBuffers) {
            buffer.vertexBuffer.close();
            // Maybe you shouldn't close the index buffer.
            // Causes Segmentation Faults in vanilla MC code.
            //buffer.indexBuffer.close();
        }
        gpuBuffers.clear();
    }

    void upload(MeshData data, ChunkSectionLayer chunkSectionLayer) {
        if (chunkSectionLayer.sortOnUpload()) {
            data.sortQuads(sharedBuffer, RenderSystem.getProjectionType().vertexSorting());
        }
        RenderPipeline pipeline = chunkSectionLayer.pipeline();

        GpuDevice device = RenderSystem.getDevice();

        GpuBuffer vertexBuffer = device.createBuffer(() -> "Lighty vertex buffer for " + pipeline.getVertexFormat(), BUFFER_TYPE_VERTEX, data.vertexBuffer());
        GpuBuffer indexBuffer;
        VertexFormat.IndexType indexType;

        if (data.indexBuffer() == null) {
            indexBuffer = null;
            indexType = null;
        } else {
            indexBuffer = device.createBuffer(() -> "Lighty index buffer for" + pipeline.getVertexFormat(), BUFFER_TYPE_INDEX, data.indexBuffer());
            indexType = data.drawState().indexType();
        }

        gpuBuffers.add(new Data(vertexBuffer, indexBuffer, indexType, data.drawState().indexCount()));
        data.close();
    }

    List<Data> getGpuBuffers() {
        return gpuBuffers;
    }
}

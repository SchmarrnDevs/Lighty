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

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.ArrayList;
import java.util.List;

public class BufferHolder {
    // List because we can hold multiple gpuBuffers from different data providers
    private final List<GpuBuffer> gpuBuffers;
    private final List<GpuBuffer> indexBuffers;
    private final List<VertexFormat.IndexType> indexTypes;

    BufferHolder() {
        gpuBuffers = new ArrayList<>();
        indexBuffers = new ArrayList<>();
        indexTypes = new ArrayList<>();
    }

    boolean isValid() {
        return !gpuBuffers.isEmpty();
    }

    void close() {
        for (var buffer : gpuBuffers) {
            buffer.close();
        }
        for (var buffer : indexBuffers) {
            if (buffer != null) {
                buffer.close();
            }
        }
        gpuBuffers.clear();
        indexBuffers.clear();
        indexTypes.clear();
    }

    void upload(MeshData buffer) {
        gpuBuffers.add(RenderSystem.getDevice().createBuffer(() -> "lighty vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, buffer.vertexBuffer()));
        if (buffer.indexBuffer() != null) {
            indexBuffers.add(
                    RenderSystem.getDevice().createBuffer(
                            () -> "lighty index buffer",
                            BufferType.INDICES,
                            BufferUsage.STATIC_WRITE,
                            buffer.indexBuffer()
                    )
            );
            indexTypes.add(buffer.drawState().indexType());
        } else {
            indexBuffers.add(null);
            indexTypes.add(null);
        }


        buffer.close();
    }

    List<GpuBuffer> getGpuBuffers() {
        return gpuBuffers;
    }
    List<GpuBuffer> getIndexBuffers() {
        return indexBuffers;
    }
    List<VertexFormat.IndexType> getIndexTypes() {
        return indexTypes;
    }
}

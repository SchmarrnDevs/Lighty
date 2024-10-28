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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class BufferHolder {
    private final List<VertexBuffer> vertexBuffers;

    BufferHolder() {
        vertexBuffers = new ArrayList<>();
    }

    boolean isValid() {
        return !vertexBuffers.isEmpty();
    }

    void close() {
        for (var buffer : vertexBuffers) {
            buffer.close();
        }
        vertexBuffers.clear();
    }

    void upload(MeshData buffer) {
        if (buffer == null) {
            // Don't upload
            return;
        }

        var vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        VertexBuffer.unbind();

        vertexBuffers.add(vertexBuffer);
    }

    void draw(Matrix4f positionMatrix, Matrix4f projectionMatrix, ShaderInstance shader) {
        for (var vertexBuffer : vertexBuffers) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(positionMatrix, projectionMatrix, shader);
            VertexBuffer.unbind();
        }
    }
}

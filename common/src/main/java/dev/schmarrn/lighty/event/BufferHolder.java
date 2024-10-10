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

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BufferHolder {
    @Nullable
    private VertexBuffer vertexBuffer;
    private boolean isEmpty;

    BufferHolder() {
        vertexBuffer = null;
        isEmpty = false;
    }

    boolean isValid() {
        return isEmpty || vertexBuffer != null;
    }

    void close() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
            isEmpty = false;
        }
    }

    void upload(MeshData buffer) {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        if (buffer == null) {
            // Don't upload
            isEmpty = true;
            vertexBuffer = null;
        } else {
            isEmpty = false;
            vertexBuffer = new VertexBuffer(BufferUsage.DYNAMIC_WRITE);
            vertexBuffer.bind();
            vertexBuffer.upload(buffer);
            VertexBuffer.unbind();
        }
    }

    void draw(Matrix4f positionMatrix, Matrix4f projectionMatrix, CompiledShaderProgram shader) {
        if (vertexBuffer != null) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(positionMatrix, projectionMatrix, shader);
            VertexBuffer.unbind();
        }
    }
}

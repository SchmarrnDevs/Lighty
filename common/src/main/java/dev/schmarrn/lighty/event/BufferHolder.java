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
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BufferHolder {
    @Nullable
    private VertexBuffer vertexBuffer;

    BufferHolder() {
        vertexBuffer = null;
    }

    boolean isValid() {
        return vertexBuffer != null;
    }

    void close() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    void upload(BufferBuilder.RenderedBuffer buffer) {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        if (buffer.isEmpty()) {
            // Don't upload
            vertexBuffer = null;
        } else {
            vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            vertexBuffer.bind();
            vertexBuffer.upload(buffer);
            VertexBuffer.unbind();
        }
    }

    void draw(Matrix4f positionMatrix, Matrix4f projectionMatrix, ShaderInstance shader) {
        if (vertexBuffer != null) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(positionMatrix, projectionMatrix, shader);
            VertexBuffer.unbind();
        }
    }
}

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
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        VertexBuffer.unbind();
    }

    void draw(Matrix4f positionMatrix, Matrix4f projectionMatrix, ShaderInstance shader) {
        if (vertexBuffer != null) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(positionMatrix, projectionMatrix, shader);
            VertexBuffer.unbind();
        }
    }
}

package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/// Inspired by ChunkSectionLayer (26.1.2)
public enum OverlaySectionLayer {
    LINES(LightyPipelines.LINES, 4194304, false),
    CUTOUT(LightyPipelines.TERRAIN_CUTOUT, 4194304, false),
    TRANSLUCENT(LightyPipelines.TERRAIN_TRANSLUCENT, 786432, true);

    private final RenderPipeline pipeline;
    private final int bufferSize;
    private final boolean translucent;
    private final String label;

    OverlaySectionLayer(RenderPipeline pipeline, int bufferSize, boolean translucent) {
        this.pipeline = pipeline;
        this.bufferSize = bufferSize;
        this.translucent = translucent;
        this.label = this.toString().toLowerCase(Locale.ROOT);
    }

    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public String label() {
        return this.label;
    }

    public boolean translucent() {
        return this.translucent;
    }

    public VertexFormat vertexFormat() {
        return this.pipeline.getVertexFormat();
    }

    public RenderTarget outputTarget() {
        Minecraft minecraft = Minecraft.getInstance();

        RenderTarget renderTarget = switch (this) {
            case TRANSLUCENT -> minecraft.levelRenderer.getTranslucentTarget();
            default -> minecraft.getMainRenderTarget();
        };
        return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
    }
}

package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

public class LightyPipelines {
    public static VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    public static RenderPipeline.Snippet TERRAIN_SNIPPET;
    public static RenderPipeline TERRAIN_TRANSLUCENT;
    public static RenderPipeline TERRAIN_CUTOUT;
}

package dev.schmarrn.lighty.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

public class LightyPipelines {
    public static VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    public static VertexFormat POSITION_COLOR_NORMAL;

    public static RenderPipeline.Snippet TERRAIN_TRANSLUCENT_SNIPPET;
    public static RenderPipeline.Snippet TERRAIN_CUTOUT_SNIPPET;
    public static RenderPipeline.Snippet LINES_SNIPPET;
    public static RenderPipeline TERRAIN_TRANSLUCENT;
    public static RenderPipeline TERRAIN_CUTOUT;
    public static RenderPipeline LINES;
}

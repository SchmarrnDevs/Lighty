package dev.schmarrn.lighty.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.core.LightyPipelines;
import dev.schmarrn.lighty.core.LightyVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {

    @Shadow @Final public static RenderPipeline.Snippet GLOBALS_SNIPPET;
    @Unique
    private static final BlendFunction LIGHTY_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void lighty$initRenderPipelines(CallbackInfo ci) {
        RenderPipeline.Snippet FOG_SNIPPET = RenderPipeline.builder().withUniform("Fog", UniformType.UNIFORM_BUFFER).buildSnippet();
        RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET = RenderPipeline.builder()
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .buildSnippet();
        RenderPipeline.Snippet MATRICES_FOG_SNIPPET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET, FOG_SNIPPET).buildSnippet();
        RenderPipeline.Snippet GENERIC_BLOCKS_SNIPPET = RenderPipeline.builder(FOG_SNIPPET)
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withVertexFormat(LightyVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
                .buildSnippet();
        LightyPipelines.TERRAIN_SNIPPET = RenderPipeline.builder(GENERIC_BLOCKS_SNIPPET)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("ChunkSection", UniformType.UNIFORM_BUFFER)
                .withVertexShader("core/terrain")
                .withFragmentShader("core/terrain")
                .buildSnippet();

        LightyPipelines.LINES_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET, GLOBALS_SNIPPET)
                .withVertexShader("core/rendertype_lines")
                .withFragmentShader("core/rendertype_lines")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withVertexFormat(LightyVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                .buildSnippet();

        LightyPipelines.TERRAIN_TRANSLUCENT = RenderPipeline.builder(LightyPipelines.TERRAIN_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "pipeline/translucent_terrain"))
                .withBlend(LIGHTY_BLEND)
                //.withShaderDefine("ALPHA_CUTOUT", 0.01F)
                .build();

        LightyPipelines.TERRAIN_CUTOUT = RenderPipeline.builder(LightyPipelines.TERRAIN_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "pipeline/cutout_terrain"))
                .withShaderDefine("ALPHA_CUTOUT", 0.5F)
                .build();

        LightyPipelines.LINES = RenderPipeline.builder(LightyPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Lighty.MOD_ID, "pipeline/lines"))
                .build();

        // If Iris is loaded, register the pipelines with iris as well
        UtilDefinition.INSTANCE.registerPipelinesWithIris();
    }
}

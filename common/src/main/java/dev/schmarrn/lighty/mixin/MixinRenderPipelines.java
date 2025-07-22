package dev.schmarrn.lighty.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.compat.IrisCompat;
import dev.schmarrn.lighty.core.LightyPipelines;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPipelines.class)
public class MixinRenderPipelines {

    @Unique
    private static final BlendFunction LIGHTY_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void initLightyPipelines(CallbackInfo ci) {

        RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET = RenderPipeline.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).buildSnippet();
        RenderPipeline.Snippet FOG_SNIPPET = RenderPipeline.builder().withUniform("Fog", UniformType.UNIFORM_BUFFER).buildSnippet();
        RenderPipeline.Snippet MATRICES_FOG_SNIPPET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET, FOG_SNIPPET).buildSnippet();

        LightyPipelines.POSITION_COLOR_TEXTURE_LIGHT_NORMAL = VertexFormat.builder()
                .add("Position", VertexFormatElement.POSITION)
                .add("Color", VertexFormatElement.COLOR)
                .add("Texture", VertexFormatElement.UV)
                .add("Light", VertexFormatElement.UV2)
                .add("Normal", VertexFormatElement.NORMAL)
                .build();

        LightyPipelines.TERRAIN_TRANSLUCENT_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
                .withVertexShader("core/terrain")
                .withFragmentShader("core/terrain")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withVertexFormat(LightyPipelines.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.Mode.QUADS)
                .withBlend(BlendFunction.TRANSLUCENT)
                .buildSnippet();

        LightyPipelines.TERRAIN_CUTOUT_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
                .withVertexShader("core/terrain")
                .withFragmentShader("core/terrain")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withVertexFormat(LightyPipelines.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.Mode.QUADS)
                .withBlend(LIGHTY_BLEND)
                .buildSnippet();


        LightyPipelines.TERRAIN_TRANSLUCENT = RenderPipeline.builder(LightyPipelines.TERRAIN_TRANSLUCENT_SNIPPET)
                .withLocation(Lighty.MOD_ID + "pipeline/translucent")
                .build();

        LightyPipelines.TERRAIN_CUTOUT = RenderPipeline.builder(LightyPipelines.TERRAIN_CUTOUT_SNIPPET)
                .withLocation(Lighty.MOD_ID + "pipeline/cutout")
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .build();

        // Try registering with Iris
        //IrisCompat.INSTANCE.registerPipelines();
    }
}

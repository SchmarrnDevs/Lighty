package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.core.LightyPipelines;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class CrossRenderer implements OverlayRenderer {
    public void build(ClientLevel level, BlockPos pos, OverlayData data, VertexConsumer builder, int lightmap) {
        float x1 = data.rPos().getX();
        float x2 = data.rPos().getX() + 1f;
        float y  = data.rPos().getY() + 1.005f + data.yOffset();
        float z1 = data.rPos().getZ();
        float z2 = data.rPos().getZ() + 1f;

        builder.addVertex(x1, y, z1).setColor(data.color()).setNormal(1f, 0f, 1f);
        builder.addVertex(x2, y, z2).setColor(data.color()).setNormal(1f, 0f, 1f);
        builder.addVertex(x1, y, z2).setColor(data.color()).setNormal(1f, 0f, -1f);
        builder.addVertex(x2, y, z1).setColor(data.color()).setNormal(1f, 0f, -1f);
    }

    @Override
    public RenderPipeline getPipeline() {
        return LightyPipelines.LINES;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return Config.CROSS_TEXTURE.getValue();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "renderer_cross");
    }

    @Override
    public VertexFormat getVertexFormat() {
        return LightyPipelines.POSITION_COLOR_NORMAL;
    }

    @Override
    public VertexFormat.Mode getVertexFormatMode() {
        return VertexFormat.Mode.LINES;
    }

    public static void init() {
        var dp = new CrossRenderer();
        ModeManager.registerRenderer(dp.getResourceLocation(), dp);
    }
}

package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;

public class BoringCrossMode extends LightyMode {

    @Override
    public void beforeCompute(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void compute(ClientLevel world, BlockPos pos, BufferBuilder builder) {
        if (world.getBlockState(pos).isAir() && !world.getBlockState(pos.below()).isAir()) {
            int blockLight = world.getBrightness(LightLayer.BLOCK, pos);
            int skyLight = world.getBrightness(LightLayer.SKY, pos);

            int data = LightyColors.getSafeARGB();

            if (blockLight <= Config.getBlockThreshold()) {
                if (skyLight <= Config.getSkyThreshold()) {
                    data = LightyColors.getDangerARGB();
                } else {
                    data = LightyColors.getWarningARGB();
                }
            }
            float x1 = pos.getX();
            float x2 = pos.getX() + 1f;
            float y = pos.getY() + 0.005f;
            float z1 = pos.getZ();
            float z2 = pos.getZ() + 1f;

            builder.vertex(x1, y, z1).color(data).endVertex();
            builder.vertex(x2, y, z2).color(data).endVertex();
            builder.vertex(x2, y, z1).color(data).endVertex();
            builder.vertex(x1, y, z2).color(data).endVertex();
        }
    }

    @Override
    public void beforeRendering() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void afterRendering() {
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.0F);
    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "boring_cross_mode"), new BoringCrossMode());
    }
}

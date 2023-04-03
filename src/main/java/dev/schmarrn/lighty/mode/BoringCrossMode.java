package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
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

            if (blockLight == 0) {
                if (skyLight == 0) {
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

    //    @Override
//    public void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client) {
//        if (cachedBuffer == null) {
//            cachedBuffer = new VertexBuffer();
//        }
//        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//        RenderSystem.lineWidth(1.0f);
//
//        Camera camera = client.gameRenderer.getCamera();
//        MatrixStack matrixStack = worldRenderContext.matrixStack();
//        matrixStack.push();
//        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
//        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
//        Matrix4f projectionMatrix = worldRenderContext.projectionMatrix();
//
//        if (reRender) {
//            Lighty.LOGGER.info("Rebuilding Buffer");
//            reRender = false;
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder builder = tessellator.getBuffer();
//
//            builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
//            cache.forEach((pos, color) -> {
//                float x1 = pos.getX();
//                float x2 = pos.getX() + 1f;
//                float y = pos.getY() + 0.005f;
//                float z1 = pos.getZ();
//                float z2 = pos.getZ() + 1f;
//
//
//                builder.vertex(x1, y, z1).color(color).next();
//                builder.vertex(x2, y, z2).color(color).next();
//                builder.vertex(x2, y, z1).color(color).next();
//                builder.vertex(x1, y, z2).color(color).next();
//            });
//            cachedBuffer.upload(builder.end());
//        }
//
//        cachedBuffer.draw(positionMatrix, projectionMatrix, GameRenderer.getPositionColorProgram());
//
//        matrixStack.pop();
//        RenderSystem.lineWidth(1.0F);
//    }

    public static void init() {
        ModeManager.registerMode(new ResourceLocation(Lighty.MOD_ID, "boring_cross_mode"), new BoringCrossMode());
    }
}

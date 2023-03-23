package dev.schmarrn.lighty.mode;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

public class BoringCrossMode extends LightyMode {

    @Override
    public void compute(ClientWorld world, BlockPos pos, BufferBuilder builder) {
        if (world.getBlockState(pos).isAir() && !world.getBlockState(pos.down()).isAir()) {
            int blockLight = world.getLightLevel(LightType.BLOCK, pos);
            int skyLight = world.getLightLevel(LightType.SKY, pos);

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

            builder.vertex(x1, y, z1).color(data).next();
            builder.vertex(x2, y, z2).color(data).next();
            builder.vertex(x2, y, z1).color(data).next();
            builder.vertex(x1, y, z2).color(data).next();
        }
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
        ModeManager.registerMode(new Identifier(Lighty.MOD_ID, "boring_cross_mode"), new BoringCrossMode());
    }
}

package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.*;

public class Compute {
    private static final HashSet<SectionPos> toBeUpdated = new HashSet<>();
    private static final HashSet<SectionPos> toBeRemoved = new HashSet<>();
    private static final Map<SectionPos, VertexBuffer> cachedBuffers = new LinkedHashMap<>();
    private static ChunkPos playerPos = null;

    public static void clear() {
        toBeUpdated.clear();
        cachedBuffers.clear();
    }

    public static void updateSubChunk(SectionPos pos) {
        toBeUpdated.add(pos);
    }

    private static SectionPos fromVec3i(Vec3i vec) {
        return SectionPos.of(vec.getX(), vec.getY(), vec.getZ());
    }

    private static void updateUpDown(SectionPos pos) {
        updateSubChunk(pos);
        updateSubChunk(fromVec3i(pos.above()));
        updateSubChunk(fromVec3i(pos.below()));
    }

    public static void updateSubChunkAndSurrounding(SectionPos pos) {
        updateUpDown(pos);
        updateUpDown(fromVec3i(pos.south()));
        updateUpDown(fromVec3i(pos.north()));
        updateUpDown(fromVec3i(pos.east()));
        updateUpDown(fromVec3i(pos.east().north()));
        updateUpDown(fromVec3i(pos.east().south()));
        updateUpDown(fromVec3i(pos.west()));
        updateUpDown(fromVec3i(pos.west().north()));
        updateUpDown(fromVec3i(pos.west().south()));
    }

    private static VertexBuffer buildChunk(LightyMode mode, SectionPos chunkPos, BufferBuilder builder, ClientLevel world) {
        mode.beforeCompute(builder);
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = chunkPos.origin().offset(x, y, z);

                    mode.compute(world, pos, builder);
                }
            }
        }

        VertexBuffer vertexBuffer = cachedBuffers.get(chunkPos);
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        vertexBuffer.bind();
        vertexBuffer.upload(builder.end());
        VertexBuffer.unbind();

        mode.afterCompute();
        return vertexBuffer;
    }

    public static void computeCache(Minecraft client) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        LocalPlayer player = client.player;
        ClientLevel world = client.level;
        if (player == null || world == null) {
            return;
        }

        Tesselator tesselator = Tesselator.getInstance();
        if (!toBeUpdated.isEmpty()) {
            Lighty.LOGGER.info("To be computed: {}", toBeUpdated.size());
        }

        playerPos = new ChunkPos(player.blockPosition());

        for (SectionPos sectionPos : toBeRemoved) {
            var buf = cachedBuffers.remove(sectionPos);
            if (buf != null) {
                buf.close();
            }
        }
        toBeRemoved.clear();

        ChunkPos.rangeClosed(playerPos, 2).forEach(chunkPos -> {
            for (int i = 0; i < world.getSectionsCount(); ++i) {
                var chunkSection = SectionPos.of(chunkPos, world.getMinSection() + i);
                if (!cachedBuffers.containsKey(chunkSection) || toBeUpdated.contains(chunkSection)) {
                    cachedBuffers.compute(chunkSection, (pos, vertexBuffer) -> {
                        if (vertexBuffer != null) {
                            vertexBuffer.close();
                        }
                        return buildChunk(mode, pos, tesselator.getBuilder(), world);
                    });
                }
            }
        });
    }

    public static void render(WorldRenderContext worldRenderContext) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        Frustum frustum = worldRenderContext.frustum();
        if (frustum == null) {
            return;
        }

        if (playerPos == null) {
            return;
        }

        mode.beforeRendering();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        PoseStack matrixStack = worldRenderContext.matrixStack();
        matrixStack.pushPose();
        matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        Matrix4f positionMatrix = matrixStack.last().pose();
        Matrix4f projectionMatrix = worldRenderContext.projectionMatrix();
        cachedBuffers.forEach(((chunkPos, cachedBuffer) -> {
            int sqX = (chunkPos.x() - playerPos.x) * (chunkPos.x() - playerPos.x);
            int sqZ =  (chunkPos.z() - playerPos.z) * (chunkPos.z() - playerPos.z);
            if (sqX > 4 || sqZ > 4) {
                toBeRemoved.add(chunkPos);
            } else if (frustum.isVisible(new AABB(chunkPos.minBlockX(), chunkPos.minBlockY(), chunkPos.minBlockZ(), chunkPos.maxBlockX(), chunkPos.maxBlockY(), chunkPos.maxBlockZ()))) {
                cachedBuffer.bind();
                cachedBuffer.drawWithShader(positionMatrix, projectionMatrix, RenderSystem.getShader());
            }
        }));
        VertexBuffer.unbind();

        matrixStack.popPose();

        mode.afterRendering();
    }

    private Compute() {}
}

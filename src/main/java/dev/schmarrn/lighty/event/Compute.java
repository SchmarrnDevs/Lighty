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
    private static final Queue<SectionPos> toBeComputed = new ArrayDeque<>();
    private static final Queue<SectionPos> toBeUpdated = new ArrayDeque<>();
    private static final Set<ChunkPos> loadedChunks = new HashSet<>();

    public static void clear() {
        toBeComputed.clear();
        toBeUpdated.clear();
        cachedBuffers.clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        for (ChunkPos pos : loadedChunks) {
            addChunk(level, pos);
        }
    }

    public static void addChunk(ClientLevel world, ChunkPos pos) {
        loadedChunks.add(pos);
        for (int i = 0; i < world.getSectionsCount(); ++i) {
            Compute.addSubChunk(SectionPos.of(pos, world.getMinSection() + i));
        }
    }

    public static void removeChunk(ClientLevel world, ChunkPos pos) {
        loadedChunks.remove(pos);
        for (int i = 0; i < world.getSectionsCount(); ++i) {
            Compute.removeSubChunk(SectionPos.of(pos, world.getMinSection() + i));
        }
    }

    public static void addSubChunk(SectionPos pos) {
        if (toBeComputed.contains(pos)) return;
        toBeComputed.add(pos);
    }

    public static void updateSubChunk(SectionPos pos) {
        toBeComputed.remove(pos);
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

    public static void removeSubChunk(SectionPos pos) {
        toBeComputed.remove(pos);
        VertexBuffer buffer = cachedBuffers.remove(pos);
        if (buffer != null) {
            buffer.close();
        }
    }

    private static final Map<SectionPos, VertexBuffer> cachedBuffers = new HashMap<>();

    private static void buildChunk(LightyMode mode, SectionPos chunkPos, BufferBuilder builder, ClientLevel world) {
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
        vertexBuffer = new VertexBuffer();
        vertexBuffer.bind();
        vertexBuffer.upload(builder.end());
        VertexBuffer.unbind();

        cachedBuffers.put(chunkPos, vertexBuffer);
        mode.afterCompute();
    }

    public static void computeCache(Minecraft client) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        LocalPlayer player = client.player;
        ClientLevel world = client.level;
        if (player == null || world == null) {
            return;
        }

        SectionPos chunkPos;
        Tesselator tesselator = Tesselator.getInstance();

        int updatedChunks = 0;
        while ((chunkPos = toBeUpdated.poll()) != null) {
            ++updatedChunks;

            buildChunk(mode, chunkPos, tesselator.getBuilder(), world);
        }

        int chunksPerTick = 50 - updatedChunks;
        while (chunksPerTick-- > 0 && (chunkPos = toBeComputed.poll()) != null){
            buildChunk(mode, chunkPos, tesselator.getBuilder(), world);
        }

        Lighty.LOGGER.info("Computed: {}", 50 - chunksPerTick);
    }

    public static void render(WorldRenderContext worldRenderContext) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        Frustum frustum = worldRenderContext.frustum();
        if (frustum == null) {
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
            if (frustum.isVisible(new AABB(chunkPos.minBlockX(), chunkPos.minBlockY(), chunkPos.minBlockZ(), chunkPos.maxBlockX(), chunkPos.maxBlockY(), chunkPos.maxBlockZ()))) {
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

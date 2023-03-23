package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;

import java.util.*;

public class Compute {
    private static final Queue<ChunkSectionPos> toBeComputed = new ArrayDeque<>();
    private static final Queue<ChunkSectionPos> toBeUpdated = new ArrayDeque<>();

    public static void addChunk(ClientWorld world, ChunkPos pos) {
        for (int i = 0; i < world.countVerticalSections(); ++i) {
                Compute.addSubChunk(ChunkSectionPos.from(pos, world.getBottomSectionCoord() + i));
        }
    }

    public static void removeChunk(ClientWorld world, ChunkPos pos) {
        for (int i = 0; i < world.countVerticalSections(); ++i) {
            Compute.removeSubChunk(ChunkSectionPos.from(pos, world.getBottomSectionCoord() + i));
        }
    }

    public static void addSubChunk(ChunkSectionPos pos) {
        toBeComputed.add(pos);
    }

    public static void updateSubChunk(ChunkSectionPos pos) {
        toBeComputed.remove(pos);
        toBeUpdated.add(pos);
    }

    private static ChunkSectionPos fromVec3i(Vec3i vec) {
        return ChunkSectionPos.from(vec.getX(), vec.getY(), vec.getZ());
    }

    private static void updateUpDown(ChunkSectionPos pos) {
        updateSubChunk(pos);
        updateSubChunk(fromVec3i(pos.up()));
        updateSubChunk(fromVec3i(pos.down()));
    }

    public static void updateSubChunkAndSurrounding(ChunkSectionPos pos) {
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

    public static void removeSubChunk(ChunkSectionPos pos) {
        toBeComputed.remove(pos);
        VertexBuffer buffer = cachedBuffers.remove(pos);
        if (buffer != null) {
            buffer.close();
        }
    }

    private static final Map<ChunkSectionPos, VertexBuffer> cachedBuffers = new HashMap<>();

    private static void buildChunk(LightyMode mode, ChunkSectionPos chunkPos, BufferBuilder builder, ClientWorld world) {
        builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        mode.beforeCompute();
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = chunkPos.getMinPos().add(x, y, z);

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

    public static void computeCache(MinecraftClient client) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }

        ChunkSectionPos chunkPos;
        Tessellator tessellator = Tessellator.getInstance();

        int updatedChunks = 0;
        while ((chunkPos = toBeUpdated.poll()) != null) {
            ++updatedChunks;

            buildChunk(mode, chunkPos, tessellator.getBuffer(), world);
        }

        int chunksPerTick = 50 - updatedChunks;
        while (chunksPerTick-- > 0 && (chunkPos = toBeComputed.poll()) != null){
            buildChunk(mode, chunkPos, tessellator.getBuffer(), world);
        }

        Lighty.LOGGER.info("Computed: {}", 50 - chunksPerTick);
    }

    public static void render(WorldRenderContext worldRenderContext) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(1.0f);

        Frustum frustum = worldRenderContext.frustum();
        if (frustum == null) {
            return;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        matrixStack.push();
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        Matrix4f projectionMatrix = worldRenderContext.projectionMatrix();


        cachedBuffers.forEach(((chunkPos, cachedBuffer) -> {
            if (frustum.isVisible(new Box(chunkPos.getMinX(), chunkPos.getMinY(), chunkPos.getMinZ(), chunkPos.getMaxX(), chunkPos.getMaxY(), chunkPos.getMaxZ()))) {
                cachedBuffer.bind();
                cachedBuffer.draw(positionMatrix, projectionMatrix, GameRenderer.getPositionColorProgram());
                VertexBuffer.unbind();
            }
        }));

        matrixStack.pop();
        RenderSystem.lineWidth(1.0F);
    }

    private Compute() {}
}

package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Compute {
    // Based on some observations, the hashset size only exceeds around 400 elements
    // when spectating through your world at max speed at an overlay distance of 2 chunks.
    // That's why I chose 550 as a reasonable default - not too big (a section pos is just 3 ints after all),
    // but it should be enough to avoid rehashing at a reasonable overlay distance
    private static final int INITIAL_HASHSET_CAPACITY = 100;
    private static final HashSet<SectionPos> toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static HashSet<SectionPos> toBeRemoved = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static final Map<SectionPos, VertexBuffer> cachedBuffers = new HashMap<>();
    private static ChunkPos playerPos = null;

    private static boolean outOfRange(SectionPos pos) {
        if (playerPos == null) {
            return true;
        }
        int sqX = (pos.x() - playerPos.x) * (pos.x() - playerPos.x);
        int sqZ =  (pos.z() - playerPos.z) * (pos.z() - playerPos.z);

        return sqX > 4 || sqZ > 4;
    }

    public static void clear() {
        toBeUpdated.clear();
        cachedBuffers.clear();
    }

    public static void updateSubChunk(SectionPos pos) {
        if (outOfRange(pos)) {
            return;
        }

        toBeUpdated.add(pos);
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

        ClientLevel world = client.level;

        if (client.player == null || world == null) {
            return;
        }

        playerPos = new ChunkPos(client.player.blockPosition());

        for (SectionPos sectionPos : toBeUpdated) {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            }
        }

        for (SectionPos sectionPos : toBeRemoved) {
            toBeUpdated.remove(sectionPos);
            var buf = cachedBuffers.remove(sectionPos);
            if (buf != null) {
                buf.close();
            }
        }
        // Instead of clear (which goes through every element, setting it null), just throw away the whole
        // old hashset - should be faster in theory
        toBeRemoved = new HashSet<>(INITIAL_HASHSET_CAPACITY);

        ChunkPos.rangeClosed(playerPos, 2).forEach(chunkPos -> {
            for (int i = 0; i < world.getSectionsCount(); ++i) {
                var chunkSection = SectionPos.of(chunkPos, world.getMinSection() + i);
                if (!cachedBuffers.containsKey(chunkSection) || toBeUpdated.contains(chunkSection)) {
                    toBeUpdated.remove(chunkSection);
                    cachedBuffers.compute(chunkSection, (pos, vertexBuffer) -> {
                        if (vertexBuffer != null) {
                            vertexBuffer.close();
                        }
                        return buildChunk(mode, pos, new BufferBuilder(0x200000), world);
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
            if (outOfRange(chunkPos)) {
                toBeRemoved.add(chunkPos);
            } else if (frustum.isVisible(new AABB(chunkPos.minBlockX()-1, chunkPos.minBlockY()-1, chunkPos.minBlockZ()-1, chunkPos.maxBlockX()+1, chunkPos.maxBlockY()+1, chunkPos.maxBlockZ()+1))) {
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

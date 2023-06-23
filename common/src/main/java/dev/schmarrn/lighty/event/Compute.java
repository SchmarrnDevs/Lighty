package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Compute {
    // Based on some observations, the hashset size only exceeds around 400 elements
    // when spectating through your world at max speed at an overlay distance of 2 chunks.
    // That's why I chose 550 as a reasonable default - not too big (a section pos is just 3 ints after all),
    // but it should be enough to avoid rehashing at a reasonable overlay distance
    private static final int INITIAL_HASHSET_CAPACITY = 550;
    private static HashSet<SectionPos> toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static HashSet<SectionPos> toBeRemoved = new HashSet<>(INITIAL_HASHSET_CAPACITY);
    private static final Map<SectionPos, BufferHolder> cachedBuffers = new HashMap<>();
    private static ChunkPos playerPos = null;

    private static int overlayDistance = Config.getOverlayDistance();
    private static int computationDistance = Math.min(overlayDistance, Minecraft.getInstance().options.renderDistance().get());


    private static boolean outOfRange(SectionPos pos) {
        int renderDistance = Minecraft.getInstance().options.renderDistance().get();
        int renderDistanceSquared = renderDistance * renderDistance;
        int distanceSquared = overlayDistance * overlayDistance;
        if (playerPos == null) {
            return true;
        }
        // squared X and Z
        int sqX = (pos.x() - playerPos.x) * (pos.x() - playerPos.x);
        int sqZ =  (pos.z() - playerPos.z) * (pos.z() - playerPos.z);

        return sqX > distanceSquared || sqZ > distanceSquared || sqX > renderDistanceSquared || sqZ > renderDistanceSquared;
    }

    public static void clear() {
        toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        cachedBuffers.forEach((sectionPos, vertexBuffer) -> {
            // Important to avoid a Memory leak!
            vertexBuffer.close();
        });
        cachedBuffers.clear();
        overlayDistance = Config.getOverlayDistance(); // only update on clear
        computationDistance = Math.min(overlayDistance, Minecraft.getInstance().options.renderDistance().get());
    }

    public static void updateSubChunk(SectionPos pos) {
        if (outOfRange(pos)) {
            return;
        }

        toBeUpdated.add(pos);
    }

    private static BufferHolder buildChunk(LightyMode mode, SectionPos chunkPos, BufferBuilder builder, ClientLevel world) {
        mode.beforeCompute(builder);
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = chunkPos.origin().offset(x, y, z);

                    mode.compute(world, pos, builder);
                }
            }
        }

        BufferHolder buffer = cachedBuffers.get(chunkPos);
        if (buffer == null) {
            buffer = new BufferHolder();
        }
        buffer.upload(builder.end());

        mode.afterCompute();
        return buffer;
    }

    public static void computeCache(Minecraft client) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        ClientLevel world = client.level;

        if (client.player == null || world == null) {
            return;
        }

        playerPos = new ChunkPos(client.player.blockPosition());

        HashSet<SectionPos> removeFromToBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        for (SectionPos sectionPos : toBeUpdated) {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            } else {
                removeFromToBeUpdated.add(sectionPos);
                cachedBuffers.compute(sectionPos, (pos, vertexBuffer) -> {
                    if (vertexBuffer != null) {
                        vertexBuffer.close();
                    }
                    return buildChunk(mode, pos, Tesselator.getInstance().getBuilder(), world);
                });
            }
        }

        // Don't know how to remove stuff while iterating over it in java, so this has to suffice.
        for (SectionPos pos : removeFromToBeUpdated) {
            toBeUpdated.remove(pos);
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
    }

    public static void render(@Nullable Frustum frustum, PoseStack matrixStack, Matrix4f projectionMatrix) {
        LightyMode mode = ModeLoader.getCurrentMode();
        if (mode == null) return;

        if (frustum == null) {
            return;
        }

        if (playerPos == null) {
            return;
        }

        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        mode.beforeRendering();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        matrixStack.pushPose();
        matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        Matrix4f positionMatrix = matrixStack.last().pose();

        for (int x = -computationDistance + 1; x < computationDistance; ++x) {
            for (int z = -computationDistance + 1; z < computationDistance; ++z) {
                ChunkPos chunkPos = new ChunkPos(playerPos.x + x, playerPos.z + z);
                for (int i = 0; i < world.getSectionsCount(); ++i) {
                    var chunkSection = SectionPos.of(chunkPos, world.getMinSection() + i);
                    if (outOfRange(chunkSection)) {
                        toBeRemoved.add(chunkSection);
                    } else if (frustum.isVisible(new AABB(chunkSection.origin(), chunkSection.origin().offset(15,15,15)))) {
                        if (cachedBuffers.containsKey(chunkSection)) {
                            BufferHolder cachedBuffer = cachedBuffers.get(chunkSection);
                            if (!cachedBuffer.isValid()) {
                                toBeUpdated.add(chunkSection);
                            } else {
                                cachedBuffer.draw(positionMatrix, projectionMatrix, RenderSystem.getShader());
                            }
                        } else {
                            toBeUpdated.add(chunkSection);
                        }
                    } else {
                        toBeRemoved.add(chunkSection);
                    }
                }
            }
        }

        matrixStack.popPose();

        mode.afterRendering();
    }

    private Compute() {}
}

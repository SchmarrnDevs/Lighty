// Copyright 2022-2023 The Lighty contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.schmarrn.lighty.DataProviders;
import dev.schmarrn.lighty.Renderers;
import dev.schmarrn.lighty.api.OverlayData;
import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.overlaystate.SMACH;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;

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

    private static int computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get());


    private static boolean outOfRange(SectionPos pos) {
        int computationDistanceSquared = computationDistance * computationDistance;
        if (playerPos == null) {
            return true;
        }
        // squared X and Z
        int sqX = (pos.x() - playerPos.x) * (pos.x() - playerPos.x);
        int sqZ =  (pos.z() - playerPos.z) * (pos.z() - playerPos.z);

        return sqX > computationDistanceSquared || sqZ > computationDistanceSquared;
    }

    public static void clear() {
        toBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        cachedBuffers.forEach((sectionPos, vertexBuffer) -> {
            // Important to avoid a Memory leak!
            vertexBuffer.close();
        });
        cachedBuffers.clear();
        computationDistance = Math.min(Config.OVERLAY_DISTANCE.getValue(), Minecraft.getInstance().options.renderDistance().get());
    }

    public static void updateBlockPos(BlockPos pos) {
        SectionPos spos = SectionPos.of(pos);
        if (spos.minBlockY() == pos.getY()) {
            // if we are on the y-border of a SubChunk, we need to update *both* SubChunks
            // see https://github.com/SchmarrnDevs/Lighty/issues/70
            updateSubChunk(spos.offset(0, -1, 0));
        }
        updateSubChunk(spos);
    }

    public static void updateSubChunk(SectionPos pos) {
        if (outOfRange(pos)) {
            return;
        }

        toBeUpdated.add(pos);
    }

    private static BufferHolder buildChunk(OverlayRenderer renderer, List<OverlayDataProvider> dataProviders, SectionPos chunkPos, Tesselator tesselator, ClientLevel world) {
        List<OverlayData> overlayData = new ArrayList<>();

        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos pos = chunkPos.origin().offset(x, y, z);

                    for (var dataProvider : dataProviders) {
                        var data = dataProvider.compute(world, pos);
                        if (data.valid()) {
                            overlayData.add(data);
                        }
                    }
                }
            }
        }

        BufferBuilder builder = renderer.beforeBuild(tesselator);
        int overlayBrightness = Config.OVERLAY_BRIGHTNESS.getValue();
        // the first parameter corresponds to the blockLightLevel, the second to the skyLightLevel
        int lightmap = LightTexture.pack(overlayBrightness, overlayBrightness);
        for (var data : overlayData) {
            renderer.build(world, data.pos(), data, builder, lightmap);
        }

        BufferHolder buffer = cachedBuffers.get(chunkPos);
        if (buffer == null) {
            buffer = new BufferHolder();
        }
        buffer.upload(builder.build());

        return buffer;
    }

    public static void computeCache(Minecraft client) {
        if (client.player == null) return;

        // update state machine state that's based on items etc
        SMACH.updateCompute(client);

        if (!SMACH.isEnabled()) {
            return;
        }
        List<OverlayDataProvider> dataProviders = DataProviders.getActiveProviders();
        OverlayRenderer renderer = Renderers.getRenderer();

        ClientLevel world = client.level;

        if (client.player == null || world == null) {
            return;
        }

        playerPos = new ChunkPos(client.player.blockPosition());

        cachedBuffers.forEach(((sectionPos, bufferHolder) -> {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            }
        }));

        HashSet<SectionPos> removeFromToBeUpdated = new HashSet<>(INITIAL_HASHSET_CAPACITY);
        for (SectionPos sectionPos : toBeUpdated) {
            if (outOfRange(sectionPos)) {
                toBeRemoved.add(sectionPos);
            } else {
                if (!Minecraft.getInstance().levelRenderer.isSectionCompiled(sectionPos.origin())) {
                    continue;
                }
                removeFromToBeUpdated.add(sectionPos);
                cachedBuffers.compute(sectionPos, (pos, vertexBuffer) -> {
                    if (vertexBuffer != null) {
                        // Ensure to have a clean state after building
                        vertexBuffer.close();
                    }
                    return buildChunk(renderer, dataProviders, pos, Tesselator.getInstance(), world);
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

    private static Matrix4f rotY(float theta) {
        // Sorry, but german:
        // Homogene Zustandstransformationsmatrix nach ACIN Modellbildung
        // Siehe: https://www.acin.tuwien.ac.at/file/teaching/bachelor/modellbildung/Formelsammlung_Modellbildung_2020S.pdf
        // Beinhaltet nur Rotation um die Y Achse, ohne Verschiebung.
        return new Matrix4f(
                Mth.cos(theta), 0, Mth.sin(theta), 0,
                0, 1, 0, 0,
                -Mth.sin(theta), 0, Mth.cos(theta), 0,
                0, 0, 0, 1
        );
    }

    private static Matrix4f rotX(float psi) {
        // Sorry, but german:
        // Homogene Zustandstransformationsmatrix nach ACIN Modellbildung
        // Siehe: https://www.acin.tuwien.ac.at/file/teaching/bachelor/modellbildung/Formelsammlung_Modellbildung_2020S.pdf
        // Beinhaltet nur Rotation um die X Achse, ohne Verschiebung.
        return new Matrix4f(
                1, 0, 0, 0,
                0, Mth.cos(psi), -Mth.sin(psi), 0,
                0, Mth.sin(psi), Mth.cos(psi), 0,
                0, 0, 0, 1
        );
    }

    public static void render(@Nullable Frustum frustum, PoseStack matrixStack, Matrix4f projectionMatrix) {
        if (!SMACH.isEnabled()) return;

        OverlayRenderer renderer = Renderers.getRenderer();

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

        renderer.beforeRendering();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        matrixStack.pushPose();
        matrixStack.mulPose(rotX(-camera.getXRot() * Mth.PI / 180f));
        matrixStack.mulPose(rotY(-camera.getYRot() * Mth.PI / 180f + Mth.PI));
        // only translate to the nearest SectionPos
        matrixStack.translate(-(camera.getPosition().x % 16), -(camera.getPosition().y % 16), -(camera.getPosition().z % 16));
        SectionPos camSectionPos = SectionPos.of(camera.getBlockPosition());
        // Don't ask me *why* I need this offset if the camera has negative components
        int dX = camSectionPos.getX() < 0 ? -16 : 0;
        int dY = camSectionPos.getY() < 0 ? -16 : 0;
        int dZ = camSectionPos.getZ() < 0 ? -16 : 0;
        BlockPos camOrigin = camSectionPos.origin().subtract(new Vec3i(dX, dY, dZ));

        ShaderInstance shader = RenderSystem.getShader();

        // The times 16 is just a magic number, chosen by trial and error.
        // The fog shenanigans should fix a really annoying issue: https://github.com/SchmarrnDevs/Lighty/issues/47
        // I hate this issue, multiply by FAC. Number chosen because FUCK YOU FOG, NOW WORK FOR GOD'S SAKE
        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance() * 16f * 0xFAC;
        float fogStart = renderDistance - Mth.clamp(renderDistance/10f, 4f, 64f);

        float oldFogStart = RenderSystem.getShaderFogStart();
        float oldFogEnd = RenderSystem.getShaderFogEnd();
        FogShape oldFogShape = RenderSystem.getShaderFogShape();

        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(renderDistance);
        RenderSystem.setShaderFogShape(FogShape.CYLINDER);

        for (int x = -computationDistance + 1; x < computationDistance; ++x) {
            for (int z = -computationDistance + 1; z < computationDistance; ++z) {
                ChunkPos chunkPos = new ChunkPos(playerPos.x + x, playerPos.z + z);
                for (int i = 0; i < world.getSectionsCount(); ++i) {
                    var chunkSection = SectionPos.of(chunkPos, world.getMinSection() + i);
                    if (frustum.isVisible(AABB.encapsulatingFullBlocks(chunkSection.origin().offset(-1, -1, -1), chunkSection.origin().offset(16,16,16)))) {
                        if (cachedBuffers.containsKey(chunkSection)) {
                            BufferHolder cachedBuffer = cachedBuffers.get(chunkSection);
                            if (!cachedBuffer.isValid()) {
                                toBeUpdated.add(chunkSection);
                            } else {
                                BlockPos origin = chunkSection.origin();
                                BlockPos dPos = origin.subtract(camOrigin);
                                matrixStack.pushPose();
                                matrixStack.translate(dPos.getX(), dPos.getY(), dPos.getZ());
                                cachedBuffer.draw(matrixStack.last().pose(), projectionMatrix, shader);
                                matrixStack.popPose();
                            }
                        } else {
                            toBeUpdated.add(chunkSection);
                        }
                    }
                }
            }
        }

        // Reset Fog stuff
        RenderSystem.setShaderFogStart(oldFogStart);
        RenderSystem.setShaderFogEnd(oldFogEnd);
        RenderSystem.setShaderFogShape(oldFogShape);

        matrixStack.popPose();

        renderer.afterRendering();
    }

    private Compute() {}
}

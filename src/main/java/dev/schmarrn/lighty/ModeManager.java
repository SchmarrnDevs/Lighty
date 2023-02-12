package dev.schmarrn.lighty;

import dev.schmarrn.lighty.mode.LightyMode;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeManager {
    @Nullable
    private static LightyMode<?, ?> mode = null;

    private static final HashMap<Identifier, LightyMode<?, ?>> MODES = new HashMap<>();

    public static void loadMode(LightyMode<?, ?> mode) {
        if (ModeManager.mode != null) {
            ModeManager.mode.unload();
        }

        ModeManager.mode = mode;
        Compute.markDirty();
    }

    public static void registerMode(Identifier id, LightyMode<?, ?> mode) {
        MODES.put(id, mode);

        ModeSwitcherScreen.addButton(
                Text.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath()),
                Text.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath() + ".tooltip"), button -> ModeManager.loadMode(mode)
        );
    }

    public static class Compute {
        private static BlockPos oldPlayerPos = BlockPos.ORIGIN;
        private static boolean isDirty = true;

        public static void markDirty() {
            isDirty = true;
        }

        public static void computeCache(MinecraftClient client) {
            ClientPlayerEntity player = client.player;
            ClientWorld world = client.world;
            if (player == null || world == null) {
                return;
            }
            BlockPos playerPos = player.getBlockPos();

            if (mode == null) return;
            if (!oldPlayerPos.equals(playerPos)) {
                markDirty();
                oldPlayerPos = playerPos;
            }

            if (isDirty) {
                isDirty = false;
                mode.beforeCompute();
                for (int x = -16; x <= 16; ++x) {
                    for (int y = -16; y <= 16; ++y) {
                        for (int z = -16; z <= 16; ++z) {
                            BlockPos pos = playerPos.add(x, y, z);
                            mode.compute(world, pos);
                        }
                    }
                }
                mode.afterCompute();
            }
        }
    }

    public static class Render {
        private static final VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        private static final MinecraftClient client = MinecraftClient.getInstance();

        static void renderOverlay(WorldRenderContext worldRenderContext) {
            if (mode == null) return;

            ClientWorld world = client.world;
            Frustum frustum = worldRenderContext.frustum();

            if (world == null || frustum == null) {
                return;
            }

            // Render
            mode.render(worldRenderContext, world, frustum, provider, client);
        }
    }
}

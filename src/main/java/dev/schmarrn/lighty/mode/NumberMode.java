package dev.schmarrn.lighty.mode;

import dev.schmarrn.lighty.Compute;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.LightyColors;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;

public class NumberMode extends LightyMode {
    private record Data(BlockPos pos, int blockLightLevel, int skyLightLevel, double offset, int color) {}
    public static final NumberMode MODE = new NumberMode();
    private final ModeCache<BlockPos, Data> cache = new ModeCache<>();

    private NumberMode() {
        ModeSwitcherScreen.addButton(Text.of("Number Mode"), button -> {
            Lighty.mode = MODE;
            Compute.markDirty();
        });
    }

    public static boolean isBlocked(BlockState block, BlockState up, ClientWorld world, BlockPos upPos) {
        // See SpawnHelper.isClearForSpawn
        // If block with FluidState (think Kelp, Seagrass, Glowlichen underwater), disable overlay
        return (up.isFullCube(world, upPos) ||
                up.emitsRedstonePower() ||
                !up.getFluidState().isEmpty()) ||
                up.isIn(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) ||
                // MagmaBlocks caused a Crash - But Mobs can still spawn on them, I need to fix this
                block.getBlock() instanceof MagmaBlock;
    }

    @Override
    public void afterCompute() {
        cache.swap();
    }

    @Override
    public void compute(ClientWorld world, BlockPos pos) {
        BlockPos posUp = pos.up();
        BlockState up = world.getBlockState(posUp);
        Block upBlock = up.getBlock();
        BlockState block = world.getBlockState(pos);
        boolean validSpawn = upBlock.canMobSpawnInside();
        if (isBlocked(block, up, world, posUp)) {
            return;
        }
        validSpawn = validSpawn && block.allowsSpawning(world, pos, null);

        if (!validSpawn) {
            return;
        }

        int blockLightLevel = world.getLightLevel(LightType.BLOCK, posUp);
        int skyLightLevel = world.getDimension().hasSkyLight() ? world.getLightLevel(LightType.SKY, posUp) : -1;

        int color = LightyColors.GREEN;
        if (blockLightLevel == 0) {
            if (skyLightLevel == 0) {
                color = LightyColors.RED;
            } else {
                color = LightyColors.ORANGE;
            }
        }

        double offset = 0;
        if (upBlock instanceof SnowBlock) { // snow layers
            int layer = world.getBlockState(posUp).get(SnowBlock.LAYERS);
            // One layer of snow is two pixels high, with one pixel being 1/16
            offset = 2f / 16f * layer;
        } else if (upBlock instanceof CarpetBlock) {
            // Carpet is just one pixel high
            offset = 1f / 16f;
        }

        cache.put(posUp, new Data(posUp, blockLightLevel, skyLightLevel, offset, color));
    }

    @Override
    public void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client) {
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();

        TextRenderer textRenderer = client.textRenderer;
        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        for (Data data : cache.getRenderBank().values()) {
            double x = data.pos.getX() + 0.5,
                    y = data.pos.getY() + data.offset + 0.5,
                    z = data.pos.getZ() + 0.5;

            boolean overlayVisible = frustum.isVisible(new Box(data.pos));

            if (!overlayVisible) {
                continue;
            }

            matrixStack.push();
            matrixStack.translate(x, y, z);
            matrixStack.scale(1f/32f, -1f/32f, 1f/32f);

            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) -(Math.PI + camera.getYaw() / 180f * Math.PI)));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) (camera.getPitch() / 180f * Math.PI)));

            String text = data.skyLightLevel >= 0 ? data.blockLightLevel + "|" + data.skyLightLevel : data.blockLightLevel + "";

            int width = textRenderer.getWidth(text);

            textRenderer.draw(text, -width / 2f, -textRenderer.fontHeight/2f, data.color, true, matrixStack.peek().getPositionMatrix(), provider, false, 0, 0xF000F0);

            matrixStack.pop();
        }

        matrixStack.pop();
        provider.draw();
    }

    public static void init() {
        // nothing to do other than loading the class
    }
}

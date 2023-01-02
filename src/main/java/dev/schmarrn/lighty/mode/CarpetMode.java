package dev.schmarrn.lighty.mode;

import dev.schmarrn.lighty.Blocks;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.LightType;

public class CarpetMode extends LightyMode {
    public static final CarpetMode MODE = new CarpetMode();

    private CarpetMode() {
        ModeSwitcherScreen.addButton(Text.of("Carpet Mode"), button -> Lighty.mode = MODE);
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
    public void compute(BlockPos playerPos, ClientWorld world, EntityType<?> type, int x, int y, int z) {
        BlockPos pos = new BlockPos(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
        BlockPos posUp = pos.up();
        BlockState up = world.getBlockState(posUp);
        Block upBlock = up.getBlock();
        BlockState block = world.getBlockState(pos);
        boolean validSpawn = upBlock.canMobSpawnInside();
        if (isBlocked(block, up, world, posUp)) {
            return;
        }
        validSpawn = validSpawn && block.allowsSpawning(world, pos, type);

        if (!validSpawn) {
            return;
        }

        int blockLightLevel = world.getLightLevel(LightType.BLOCK, posUp);
        int skyLightLevel = world.getLightLevel(LightType.SKY, posUp);

        BlockState overlayState = Blocks.GREEN_OVERLAY.getDefaultState();
        if (blockLightLevel == 0) {
            if (skyLightLevel == 0) {
                overlayState = Blocks.RED_OVERLAY.getDefaultState();
            } else {
                overlayState = Blocks.ORANGE_OVERLAY.getDefaultState();
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

        cache.add(new LightyMode.Data(posUp, overlayState, offset));
    }

    @Override
    public void render(WorldRenderContext worldRenderContext, ClientWorld world, Frustum frustum, VertexConsumerProvider.Immediate provider, MinecraftClient client) {
        MatrixStack matrixStack = worldRenderContext.matrixStack();
        Camera camera = worldRenderContext.camera();

        VertexConsumer buffer = provider.getBuffer(RenderLayer.getTranslucent());
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        matrixStack.push();
        // Reset matrix position to 0,0,0
        matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        for (LightyMode.Data data : cache) {
            double x = data.pos.getX(),
                    y = data.pos.getY() + data.offset,
                    z = data.pos.getZ();
            boolean overlayVisible = frustum.isVisible(new Box(
                    x, y, z,
                    x + 1, y + 1f / 16f, z + 1
            ));

            if (!overlayVisible) {
                continue;
            }

            matrixStack.push();
            matrixStack.translate(x, y, z);

            blockRenderManager.renderBlock(
                    data.state,
                    data.pos,
                    world,
                    matrixStack,
                    buffer,
                    false,
                    world.random
            );

            matrixStack.pop();
        }

        matrixStack.pop();
        provider.draw();
    }

    public static void init() {
        // nothing to do other than loading the class
    }
}

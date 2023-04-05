package dev.schmarrn.lighty.mode.carpet;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyColors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class Blocks {
    public static final Block GREEN_OVERLAY = registerOverlayBlock("green_overlay", LightyColors.getSafe());
    public static final Block ORANGE_OVERLAY = registerOverlayBlock("orange_overlay", LightyColors.getWarning());
    public static final Block RED_OVERLAY = registerOverlayBlock("red_overlay", LightyColors.getDanger());

    private static Block registerOverlayBlock(String name, int color) {
        Block overlay = new OverlayBlock(BlockBehaviour.Properties.of(Material.GLASS), color);
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Lighty.MOD_ID, name), overlay);
        return overlay;
    }

    public static void init() {
        // Does nothing besides making sure the blocks get registered
    }

    private Blocks() {}
}

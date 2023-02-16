package dev.schmarrn.lighty.mode.carpet;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.LightyColors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Blocks {
    public static final Block GREEN_OVERLAY = registerOverlayBlock("green_overlay", LightyColors.GREEN);
    public static final Block ORANGE_OVERLAY = registerOverlayBlock("orange_overlay", LightyColors.ORANGE);
    public static final Block RED_OVERLAY = registerOverlayBlock("red_overlay", LightyColors.RED);

    private static Block registerOverlayBlock(String name, int color) {
        Block overlay = new OverlayBlock(AbstractBlock.Settings.of(Material.WOOL), color);
        Registry.register(Registry.BLOCK, new Identifier(Lighty.MOD_ID, name), overlay);
        return overlay;
    }

    public static void init() {
        // Does nothing besides making sure the blocks get registered
    }

    private Blocks() {}
}

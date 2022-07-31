package dev.schmarrn.lighty;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Blocks {
    public static final Block GREEN_OVERLAY = registerOverlayBlock("green_overlay");
    public static final Block ORANGE_OVERLAY = registerOverlayBlock("orange_overlay");
    public static final Block RED_OVERLAY = registerOverlayBlock("red_overlay");

    private static Block registerOverlayBlock(String name) {
        Block overlay = new Block(AbstractBlock.Settings.of(Material.WOOL));
        Registry.register(Registry.BLOCK, new Identifier(Lighty.MOD_ID, name), overlay);
        return overlay;
    }

    public static void init() {
        // Does nothing besides making sure the blocks get registered
    }
}

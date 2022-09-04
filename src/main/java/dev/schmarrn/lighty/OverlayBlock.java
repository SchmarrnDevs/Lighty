package dev.schmarrn.lighty;

import net.minecraft.block.Block;

public class OverlayBlock extends Block {
    public final int color;

    public OverlayBlock(Settings settings, int color) {
        super(settings);

        this.color = color;
    }
}

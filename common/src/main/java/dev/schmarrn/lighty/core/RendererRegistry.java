package dev.schmarrn.lighty.core;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.resources.Identifier;

import java.util.HashMap;

public class RendererRegistry {
    private static final HashMap<Identifier, OverlayRenderer> RENDERERS = new HashMap<>();

    private static OverlayRenderer renderer;

    public static void put(Identifier rl, OverlayRenderer renderer) {
        RENDERERS.put(rl, renderer);
    }

    public static OverlayRenderer getRenderer() {
        return renderer;
    }

    public static void loadRenderer(Identifier rl) {
        OverlayRenderer renderer = RENDERERS.get(rl);

        if (renderer == null) {
            Lighty.LOGGER.error("Could not find renderer with id {}! Not changing renderer.", rl);
            return;
        }

        RendererRegistry.renderer = renderer;
        Config.LAST_USED_RENDERER.setValue(rl);
        Compute.markDirty();
    }

    /**
     * Needs to be called AFTER registering all the different Lighty renderers.
     * If the requested renderer isn't loaded, default to the first registered mode.
     */
    public static void setLastUsedRenderer() {
        renderer = RENDERERS.getOrDefault(Config.LAST_USED_RENDERER.getValue(), RENDERERS.values().iterator().next());
    }
}

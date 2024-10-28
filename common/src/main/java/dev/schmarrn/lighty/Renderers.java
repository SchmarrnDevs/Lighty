package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.OverlayRenderer;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class Renderers {
    private static final HashMap<ResourceLocation, OverlayRenderer> RENDERERS = new HashMap<>();

    private static OverlayRenderer renderer;

    public static void put(ResourceLocation rl, OverlayRenderer renderer) {
        RENDERERS.put(rl, renderer);
    }

    public static OverlayRenderer getRenderer() {
        return renderer;
    }

    public static void loadRenderer(ResourceLocation rl) {
        OverlayRenderer renderer = RENDERERS.get(rl);

        if (renderer == null) {
            Lighty.LOGGER.error("Could not find renderer with id {}! Not changing renderer.", rl);
            return;
        }

        Renderers.renderer = renderer;
        Config.LAST_USED_RENDERER.setValue(rl);
        Compute.clear();
    }

    /**
     * Needs to be called AFTER registering all the different Lighty renderers.
     * If the requested renderer isn't loaded, default to the first registered mode.
     */
    public static void setLastUsedRenderer() {
        renderer = RENDERERS.getOrDefault(Config.LAST_USED_RENDERER.getValue(), RENDERERS.values().iterator().next());
    }
}

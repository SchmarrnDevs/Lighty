package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.OverlayDataProvider;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataProviders {
    private static final HashMap<ResourceLocation, OverlayDataProvider> DATA_PROVIDERS = new HashMap<>();

    private static final List<OverlayDataProvider> ACTIVE_PROVIDERS = new ArrayList<>();

    public static void put(ResourceLocation rl, OverlayDataProvider dataProvider) {
        DATA_PROVIDERS.put(rl, dataProvider);
    }

    public static void activate(ResourceLocation rl) {
        if (DATA_PROVIDERS.containsKey(rl)) {
            ACTIVE_PROVIDERS.add(DATA_PROVIDERS.get(rl));
        } else {
            Lighty.LOGGER.error("There is no OverlayDataProvider registered for {}! Not changing active OverlayDataProviders.", rl);
        }
    }

    public static void deactivate(ResourceLocation rl) {
        if (DATA_PROVIDERS.containsKey(rl)) {
            ACTIVE_PROVIDERS.remove(DATA_PROVIDERS.get(rl));
        } else {
            Lighty.LOGGER.error("There is no OverlayDataProvider registered for {}! Cannot remove {}.", rl, rl);
        }
    }

    public static void setLastActiveProviders() {
        for (var rl : Config.ACTIVE_DATA_PROVIDERS.getValue()) {
            activate(rl);
        }
    }

    public static List<OverlayDataProvider> getActiveProviders() {
        return ACTIVE_PROVIDERS;
    }
}

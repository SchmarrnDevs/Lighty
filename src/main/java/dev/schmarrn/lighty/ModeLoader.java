package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeLoader {
    @Nullable
    private static LightyMode<?, ?> mode = null;

    private static boolean enabled = false;

    private static final HashMap<Identifier, LightyMode<?, ?>> MODES = new HashMap<>();

    public static void loadMode(Identifier id) {
        LightyMode<?, ?> modeToLoad = MODES.get(id);

        if (modeToLoad == null) {
            Lighty.LOGGER.error("Trying to load unregistered mode with id {}! Not changing mode.", id);
            return;
        }

        if (ModeLoader.mode != null) {
            ModeLoader.mode.unload();
        }

        ModeLoader.mode = modeToLoad;
        Config.setLastUsedMode(id);
        Compute.markDirty();
        enable();
    }

    public static void disable() {
        enabled = false;
    }

    public static void enable() {
        enabled = true;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void put(Identifier id, LightyMode<?, ?> mode) {
        MODES.put(id, mode);
    }

    @Nullable
    public static LightyMode<?, ?> getCurrentMode() {
        if (!enabled) return null;
        return mode;
    }

    /**
     * Needs to be called AFTER registering all the different Lighty modes.
     * If the requested mode isn't loaded, default to the first registered mode.
     */
    public static void setLastUsedMode() {
        mode = MODES.getOrDefault(Config.getLastUsedMode(), MODES.values().iterator().next());
    }

    private ModeLoader() {}
}

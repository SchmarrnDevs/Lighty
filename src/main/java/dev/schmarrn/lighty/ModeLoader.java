package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeLoader {
    @Nullable
    private static LightyMode<?, ?> mode = null;

    private static boolean enabled = false;

    private static final HashMap<Identifier, LightyMode<?, ?>> MODES = new HashMap<>();

    public static void loadMode(LightyMode<?, ?> mode) {
        if (enabled) {
            ModeLoader.mode.unload();
        }

        if (mode == null) {
            enabled = false;
        } else {
            enabled = true;
            ModeLoader.mode = mode;
            Compute.markDirty();
        }
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void put(Identifier id, LightyMode<?, ?> mode) {
        // the first registered mode becomes the default mode for now,
        // later maybe remember the last mode the user chose.
        // Requires: Config stuff
        if (MODES.isEmpty()) {
            ModeLoader.mode = mode;
        }
        MODES.put(id, mode);
    }

    @Nullable
    public static LightyMode<?, ?> getCurrentMode() {
        if (!enabled) return null;
        return mode;
    }

    private ModeLoader() {}
}

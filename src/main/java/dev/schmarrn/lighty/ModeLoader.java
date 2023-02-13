package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeLoader {
    @Nullable
    private static LightyMode<?, ?> mode = null;

    private static final HashMap<Identifier, LightyMode<?, ?>> MODES = new HashMap<>();

    public static void loadMode(LightyMode<?, ?> mode) {
        if (ModeLoader.mode != null) {
            ModeLoader.mode.unload();
        }

        ModeLoader.mode = mode;
        Compute.markDirty();
    }

    public static void put(Identifier id, LightyMode<?, ?> mode) {
        MODES.put(id, mode);
    }

    @Nullable
    public static LightyMode<?, ?> getCurrentMode() {
        return mode;
    }
}

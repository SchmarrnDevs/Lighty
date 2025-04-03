package dev.schmarrn.lighty.compat;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.event.Compute;

public class IrisCompat {
    private static boolean wereShadersOn = UtilDefinition.INSTANCE.shadersEnabled();
    public static void fixIrisShaders() {
        if (wereShadersOn ^ UtilDefinition.INSTANCE.shadersEnabled()) {
            // If shaders were switched on/off, we need to re-compute the overlay
            Lighty.LOGGER.info("Iris shader state changed, need to re-compute Lighty overlay.");
            wereShadersOn = UtilDefinition.INSTANCE.shadersEnabled();
            Compute.clear();
        }
    }
}

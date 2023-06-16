package dev.schmarrn.lighty;

import net.minecraft.client.KeyMapping;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * A service for methods that have to be called from common code but are different across ModLoaders and APIs
 */
public interface UtilDefinition {
    UtilDefinition INSTANCE = load();
    KeyMapping registerKeyBinding(KeyMapping mapping);

    Path getConfigDir();

    static UtilDefinition load() {
        return ServiceLoader.load(UtilDefinition.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl found"));
    }
}

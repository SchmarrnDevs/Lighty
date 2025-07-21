package dev.schmarrn.lighty.compat;

import java.util.ServiceLoader;

public interface IrisCompat {
    IrisCompat INSTANCE = load();

    void fixIrisShaders();
    boolean hasIris();
    void registerPipelines();
    static IrisCompat load() {
        return ServiceLoader.load(IrisCompat.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl found"));
    }
}
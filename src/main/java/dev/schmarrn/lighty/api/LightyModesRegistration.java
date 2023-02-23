package dev.schmarrn.lighty.api;

public interface LightyModesRegistration {
    /**
     * Register a `lightyModesRegistration` Entrypoint in your fabric.mod.json and point it to your implementation
     * of this Interface.<br/>
     * Use `ModeManager.registerMode` to register your Modes.
     */
    void registerLightyModes();
}

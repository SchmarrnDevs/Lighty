package dev.schmarrn.lighty.api;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Used for registering your LightyModes.
 */
public class ModeManager {
    /**
     * Registers a Lighty Mode and adds a Button to enable the mode to the ModeSwitcherScreen.
     * For the Button, you need to specify `modeSwitcher.{id.getNamespace}.{id.getPath}` for the
     * Button Name, and `modeSwitcher.{id.getNamespace}.{id.getPath}.tooltip` for the Tooltip of
     * the Button.
     *
     * @param id Used to generate the translatable text resource locations
     * @param mode Your LightyMode to be registered
     */
    public static void registerMode(ResourceLocation id, LightyMode mode) {
        ModeLoader.put(id, mode);

        ModeSwitcherScreen.addButton(
                Component.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath()),
                Component.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath() + ".tooltip"), button -> ModeLoader.loadMode(id)
        );
    }

    private ModeManager() {}
}

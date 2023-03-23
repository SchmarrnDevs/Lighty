package dev.schmarrn.lighty.api;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public static void registerMode(Identifier id, LightyMode mode) {
        ModeLoader.put(id, mode);

        ModeSwitcherScreen.addButton(
                Text.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath()),
                Text.translatable("modeSwitcher." + id.getNamespace() + "." + id.getPath() + ".tooltip"), button -> ModeLoader.loadMode(id)
        );
    }

    private ModeManager() {}
}

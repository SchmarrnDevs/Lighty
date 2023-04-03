package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KeyBind {
    private static final KeyMapping enableKeyBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.lighty.enable",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "category.lighty"
    ));
    private static final KeyMapping toggleKeyBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.lighty.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "category.lighty"
    ));

    private static boolean oldKeyState = false;
    private static boolean oldToggleState = false;

    public static void handleKeyBind(Minecraft client) {
        // Get new key states
        boolean newKeyState = KeyBind.enableKeyBind.isDown();
        boolean newToggleState = KeyBind.toggleKeyBind.isDown();

        // Check for rising edges
        if (newToggleState && !KeyBind.oldToggleState) {
            ModeLoader.toggle();
        }
        if (newKeyState && !KeyBind.oldKeyState) {
            client.setScreen(new ModeSwitcherScreen());
        }

        // old state is new state
        KeyBind.oldKeyState = newKeyState;
        KeyBind.oldToggleState = newToggleState;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(KeyBind::handleKeyBind);
    }

    private KeyBind() {}
}

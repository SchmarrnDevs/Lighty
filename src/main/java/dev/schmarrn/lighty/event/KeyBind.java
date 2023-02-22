package dev.schmarrn.lighty.event;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBind {
    private static final KeyBinding enableKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lighty.enable",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "category.lighty"
    ));
    private static final KeyBinding toggleKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lighty.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "category.lighty"
    ));

    private static boolean oldKeyState = false;
    private static boolean oldToggleState = false;

    public static void handleKeyBind(MinecraftClient client) {
        // Get new key states
        boolean newKeyState = KeyBind.enableKeyBind.isPressed();
        boolean newToggleState = KeyBind.toggleKeyBind.isPressed();

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

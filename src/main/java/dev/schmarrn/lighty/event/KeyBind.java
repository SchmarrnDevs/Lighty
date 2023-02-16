package dev.schmarrn.lighty.event;

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
    private static boolean oldKeyState = false;

    public static void handleKeyBind(MinecraftClient client) {
        boolean newKeyState = KeyBind.enableKeyBind.isPressed();
        // Rising edge
        if (newKeyState && !KeyBind.oldKeyState) {
            client.setScreen(new ModeSwitcherScreen());
        }
        KeyBind.oldKeyState = newKeyState;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(KeyBind::handleKeyBind);
    }

    private KeyBind() {}
}

package dev.schmarrn.lighty;

import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
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
    private static boolean newKeyState = false;

    public static void handleKeyBind(MinecraftClient ignoredClient) {
        KeyBind.newKeyState = KeyBind.enableKeyBind.isPressed();
        // Rising edge
        if (KeyBind.newKeyState && !KeyBind.oldKeyState) {
            MinecraftClient.getInstance().setScreen(new ModeSwitcherScreen());
        }
        KeyBind.oldKeyState = KeyBind.newKeyState;
    }

    public static void init() {
        // Does nothing besides making sure the keybinds get registered
    }
}

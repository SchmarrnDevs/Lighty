package dev.schmarrn.lighty.event;

import dev.schmarrn.lighty.SMACH;
import dev.schmarrn.lighty.ui.ModeSwitcherScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

public class KeyBind {
	public static KeyBinding enable;
	public static KeyBinding toggle;

	private static boolean oldEnableState = false;
	private static boolean oldToggleState = false;

	public static void callback(InputDevice currentInputDevice) {
		Minecraft minecraft = Minecraft.getMinecraft();
		@Nullable Screen currentScreen = minecraft.currentScreen;

		if (enable.isPressed() && !oldEnableState && currentScreen == null) {
			minecraft.displayScreen(new ModeSwitcherScreen());
		}
		if (toggle.isPressed() && !oldToggleState) {
			SMACH.toggle();
		}

		oldEnableState = enable.isPressed();
		oldToggleState = toggle.isPressed();
	}

	public static void init() {
		enable = new KeyBinding("key.lighty.enable")
			.setDefault(InputDevice.keyboard, Keyboard.KEY_F6);
		toggle = new KeyBinding("key.lighty.toggle")
			.setDefault(InputDevice.keyboard, Keyboard.KEY_F7);
	}

	public static void register() {
		GameSettings.keys.add(enable);
		GameSettings.keys.add(toggle);

		OptionsPages.CONTROLS
			.withComponent(new OptionsCategory("category.lighty")
				.withComponent(new KeyBindingComponent(enable))
				.withComponent(new KeyBindingComponent(toggle)));
	}
}

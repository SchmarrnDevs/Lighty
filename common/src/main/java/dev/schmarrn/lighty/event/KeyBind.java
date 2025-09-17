// Copyright 2022-2023 The Lighty contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.schmarrn.lighty.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.overlaystate.SMACH;
import dev.schmarrn.lighty.ui.LightyScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyBind {
    private static final KeyMapping.Category keyCategory = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "lighty")
    );
    public static final KeyMapping enableKeyBind = UtilDefinition.INSTANCE.registerKeyBinding(new KeyMapping(
            "key.lighty.enable",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            keyCategory
    ));
    public static final KeyMapping toggleKeyBind = UtilDefinition.INSTANCE.registerKeyBinding(new KeyMapping(
            "key.lighty.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            keyCategory
    ));

    private static boolean oldKeyState = false;
    private static boolean oldToggleState = false;

    public static void handleKeyBind(Minecraft client) {
        // Get new key states
        boolean newKeyState = KeyBind.enableKeyBind.isDown();
        boolean newToggleState = KeyBind.toggleKeyBind.isDown();

        // Check for rising edges
        if (newToggleState && !KeyBind.oldToggleState) {
            SMACH.toggle();
        }
        if (newKeyState && !KeyBind.oldKeyState) {
            client.setScreen(new LightyScreen(null));
        }

        // old state is new state
        KeyBind.oldKeyState = newKeyState;
        KeyBind.oldToggleState = newToggleState;
    }

    public static void init() {
        //Register keybinds through static classloading
    }

    private KeyBind() {}
}

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

package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeLoader {
    @Nullable
    private static LightyMode mode = null;

    private static boolean enabled = false;

    private static final HashMap<ResourceLocation, LightyMode> MODES = new HashMap<>();

    public static void loadMode(ResourceLocation id) {
        LightyMode modeToLoad = MODES.get(id);

        if (modeToLoad == null) {
            Lighty.LOGGER.error("Trying to load unregistered mode with id {}! Not changing mode.", id);
            return;
        }

        ModeLoader.mode = modeToLoad;
        Config.setLastUsedMode(id);
        Compute.clear();
        enable();
    }

    public static void disable() {
        enabled = false;
    }

    public static void enable() {
        enabled = true;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void put(ResourceLocation id, LightyMode mode) {
        MODES.put(id, mode);
    }

    @Nullable
    public static LightyMode getCurrentMode() {
        if (!enabled) return null;
        return mode;
    }

    /**
     * Needs to be called AFTER registering all the different Lighty modes.
     * If the requested mode isn't loaded, default to the first registered mode.
     */
    public static void setLastUsedMode() {
        mode = MODES.getOrDefault(Config.getLastUsedMode(), MODES.values().iterator().next());
    }

    private ModeLoader() {}
}

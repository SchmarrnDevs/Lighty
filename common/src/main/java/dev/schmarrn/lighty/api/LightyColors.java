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

package dev.schmarrn.lighty.api;

import dev.schmarrn.lighty.config.Config;

public class LightyColors {
    private static int GREEN = 0x00FF00;
    private static int ORANGE = 0xFF6600;
    private static int RED = 0xFF0000;

    static {
        onConfigUpdate();
    }

    public static void onConfigUpdate() {
        GREEN = Config.getOverlayGreen();
        ORANGE = Config.getOverlayOrange();
        RED = Config.getOverlayRed();
    }

    public static int getSafe() {
        return GREEN;
    }

    public static int getSafeARGB() {
        return GREEN | 0xFF000000;
    }

    public static int getWarning() {
        return ORANGE;
    }

    public static int getWarningARGB() {
        return ORANGE | 0xFF000000;
    }

    public static int getDanger() {
        return RED;
    }

    public static int getDangerARGB() {
        return RED  | 0xFF000000;
    }

    public static int getARGB(int blockLightLevel, int skyLightLevel, String dimension) {
        switch (dimension) {
            case "minecraft:the_nether":
                // Artificial light is always safe
                if (blockLightLevel > Config.getNetherBlockThreshold()) return LightyColors.getSafeARGB();
                // If we don't have artificial lighting, mobs can spawn at night
                if (skyLightLevel > Config.getNetherSkyThreshold()) return LightyColors.getWarningARGB();
                // Without artificial nor skylight, mobs can always spawn
                return LightyColors.getDangerARGB();
            case "minecraft:the_end":
                // Artificial light is always safe
                if (blockLightLevel > Config.getEndBlockThreshold()) return LightyColors.getSafeARGB();
                // If we don't have artificial lighting, mobs can spawn at night
                if (skyLightLevel > Config.getEndSkyThreshold()) return LightyColors.getWarningARGB();
                // Without artificial nor skylight, mobs can always spawn
                return LightyColors.getDangerARGB();
            default:
                // Use "minecraft:overworld" for all other cases
                // Artificial light is always safe
                if (blockLightLevel > Config.getOverworldBlockThreshold()) return LightyColors.getSafeARGB();
                // If we don't have artificial lighting, mobs can spawn at night
                if (skyLightLevel > Config.getOverworldSkyThreshold()) return LightyColors.getWarningARGB();
                // Without artificial nor skylight, mobs can always spawn
                return LightyColors.getDangerARGB();
        }
    }

    public static int getGrowthARGB(int blockLightLevel, int skyLightLevel) {
        int internalLightLevel = Integer.max(blockLightLevel, skyLightLevel);
        // Above the growth threshold crops always grow
        if (internalLightLevel > Config.getFarmGrowthThreshold()) return LightyColors.getSafeARGB();
        // There is insufficient light here; crops will uproot themselves and can't be planted
        if (internalLightLevel < Config.getFarmUprootThreshold()) return LightyColors.getDangerARGB();
        // Crops can be planted but won't grow without additional light
        return LightyColors.getWarningARGB();
    }

    private LightyColors() {}
}

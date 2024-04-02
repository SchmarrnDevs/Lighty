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

    public static int getARGB(int blockLightLevel, int skyLightLevel) {
        // Artificial light is always safe
        if (blockLightLevel > Config.getBlockThreshold()) return LightyColors.getSafeARGB();
        // If we don't have artificial lighting, mobs can spawn at night
        if (skyLightLevel > Config.getSkyThreshold()) return LightyColors.getWarningARGB();
        // Without artificial nor skylight, mobs can always spawn
        return LightyColors.getDangerARGB();
    }

    public static int getGrowthARGB(int blockLightLevel, int skyLightLevel) {
        // Equal to above the growth threshold crops always grow
        if (blockLightLevel >= Config.getFarmGrowthThreshold()) return LightyColors.getSafeARGB();
        // Crops won't grow at night without artificial light
        if (skyLightLevel > Config.getFarmUprootThreshold() || blockLightLevel > Config.getFarmUprootThreshold()) {
            return LightyColors.getWarningARGB();
        }
        // There is insufficient artificial and skylight here; crops will uproot themselves and can't be planted
        return LightyColors.getDangerARGB();
    }

    private LightyColors() {}
}

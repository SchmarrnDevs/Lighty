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
    private static int getSafeARGB() {
        return Config.OVERLAY_GREEN.getValue() | 0xFF000000;
    }

    private static int getWarningARGB() {
        return Config.OVERLAY_ORANGE.getValue() | 0xFF000000;
    }

    private static int getDangerARGB() {
        return Config.OVERLAY_RED.getValue() | 0xFF000000;
    }

    public static int getARGB(int blockLightLevel, int skyLightLevel) {
        // Artificial light is always safe
        if (blockLightLevel > Config.BLOCK_THRESHOLD.getValue()) return LightyColors.getSafeARGB();
        // If we don't have artificial lighting, mobs can spawn at night
        if (skyLightLevel > Config.SKY_THRESHOLD.getValue()) return LightyColors.getWarningARGB();
        // Without artificial nor skylight, mobs can always spawn
        return LightyColors.getDangerARGB();
    }

    public static int getGrowthARGB(int blockLightLevel, int skyLightLevel) {
        int internalLightLevel = Integer.max(blockLightLevel, skyLightLevel);
        // Above the growth threshold crops always grow
        if (internalLightLevel > Config.FARM_GROWTH_THRESHOLD.getValue()) return Config.ADDITIONAL_OVERLAY_GOOD.getValue() | 0xFF000000;
        // There is insufficient light here; crops will uproot themselves and can't be planted
        if (internalLightLevel < Config.FARM_UPROOT_THRESHOLD.getValue()) return Config.ADDITIONAL_OVERLAY_BAD.getValue() | 0xFF000000;
        // Crops can be planted but won't grow without additional light
        return Config.ADDITIONAL_OVERLAY_WARN.getValue() | 0xFF000000;
    }

    private LightyColors() {}
}

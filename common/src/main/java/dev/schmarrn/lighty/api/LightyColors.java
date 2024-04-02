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
        if (skyLightLevel <= Config.getSkyThreshold()) {
            return LightyColors.getDangerARGB();
        }

        if (blockLightLevel <= Config.getBlockThreshold()) {
            return LightyColors.getWarningARGB();
        }

        return LightyColors.getSafeARGB();
    }

    public static int getGrowthARGB(int blockLightLevel) {
        if (blockLightLevel <= Config.getFarmGrowthThreshold()) {
            return LightyColors.getWarningARGB();
        }

        return LightyColors.getSafeARGB();
    }

    private LightyColors() {}
}

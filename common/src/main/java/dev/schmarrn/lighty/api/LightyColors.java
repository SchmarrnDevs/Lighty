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
    private static final int GREEN = 0x00FF00;
    private static final int ORANGE = 0xFF6600;
    private static final int RED = 0xFF0000;

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
        int color = LightyColors.getSafeARGB();

        if (blockLightLevel <= Config.getBlockThreshold()) {
            if (skyLightLevel <= Config.getSkyThreshold()) {
                color = LightyColors.getDangerARGB();
            } else {
                color = LightyColors.getWarningARGB();
            }
        }

        return color;
    }

    private LightyColors() {}
}

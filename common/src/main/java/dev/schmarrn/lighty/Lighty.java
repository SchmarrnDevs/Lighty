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

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.dataproviders.BaseDataProvider;
import dev.schmarrn.lighty.dataproviders.FarmlandDataProvider;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.renderers.CarpetRenderer;
import dev.schmarrn.lighty.renderers.CrossRenderer;
import dev.schmarrn.lighty.renderers.NumberRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lighty {
    public static final String MOD_ID = "lighty";
    public static final String MOD_NAME = "Lighty";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("Let there be {}", MOD_NAME);

        Config.init();

        BaseDataProvider.init();
        FarmlandDataProvider.init();

        CarpetRenderer.init();
        CrossRenderer.init();
        NumberRenderer.init();

        KeyBind.init();
    }

    /**
     * Called after Lighty Modes are registered
     */
    public static void postLoad() {
        DataProviders.setLastActiveProviders();
        Renderers.setLastUsedRenderer();
    }
}

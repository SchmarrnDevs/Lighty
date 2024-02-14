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

package dev.schmarrn.lighty.config;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.config.compat.Lighty2Config;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final String PATH = UtilDefinition.INSTANCE.getConfigDir().toString() + "/lighty/base.config";
    private static final Map<String, String> fileState = new HashMap<>();
    private static final Map<String, ConfigSerDe> configValues = new HashMap<>();

    public static final ResourceLocationConfig LAST_USED_MODE = new ResourceLocationConfig("lighty.last_used_mode", new ResourceLocation("lighty:carpet_mode"));

    public static final IntegerConfig SKY_THRESHOLD = new IntegerConfig("lighty.sky_threshold", 0, 0, 15);
    public static final IntegerConfig BLOCK_THRESHOLD = new IntegerConfig("lighty.block_threshold", 0, 0, 15);
    public static final IntegerConfig OVERLAY_DISTANCE = new IntegerConfig("lighty.overlay_distance", 2, 1, 32);
    public static final IntegerConfig OVERLAY_BRIGHTNESS = new IntegerConfig("lighty.overlay_brightness", 10, 0, 15);

    public static final BooleanConfig SHOW_SAFE = new BooleanConfig("lighty.show_safe", true);

    public static final ColorConfig OVERLAY_GREEN = new ColorConfig("lighty.overlay_green", 0x00FF00);
    public static final ColorConfig OVERLAY_ORANGE = new ColorConfig("lighty.overlay_orange", 0xFF6600);
    public static final ColorConfig OVERLAY_RED = new ColorConfig("lighty.overlay_red", 0xFF0000);

    private static void afterRegister(String key, ConfigSerDe type) {
        String value = fileState.getOrDefault(key, null);
        if (value != null) {
            type.deserialize(value);
        }
    }

    public static void register(String key, ConfigSerDe type) {
        configValues.put(key, type);
        afterRegister(key, type);
    }

    public static void reloadFromDisk() {
        File file = new File(PATH);
        try {
            BufferedReader bf = new BufferedReader(new FileReader(file));

            bf.lines().forEach((line) -> {
                var splits = line.split("=");
                if (splits.length == 2) {
                    fileState.putIfAbsent(splits[0].trim(), splits[1].trim());
                } else {
                    Lighty.LOGGER.warn("Too many = signs on some line in the config, good luck finding them.");
                }
            });
            bf.close();
        } catch (FileNotFoundException e) {
            Lighty.LOGGER.warn("No Lighty config found at {}, using defaults.", PATH);
        } catch (IOException e) {
            Lighty.LOGGER.error("Could not close Lighty config at {}. This should not happen, please report on GitHub. Abort. {}", PATH, e);
            throw new RuntimeException(e);
        }

        for (var entry : configValues.entrySet()) {
            afterRegister(entry.getKey(), entry.getValue());
        }
    }

    public static void save() {
        StringBuilder content = new StringBuilder();

        for (var pair : configValues.entrySet()) {
            content.append(pair.getKey()).append("=").append(pair.getValue().serialize()).append("\n");
        }

        File file = new File(PATH);
        // Create the lighty config folder if it doesn't already exist
        file.getParentFile().mkdirs();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Lighty.LOGGER.warn("Could not write Lighty config file at {}. Config changes are not saved. {}", PATH, e);
        }

    }

    public static void init() {
        // Old config
        if (Lighty2Config.exists()) {
            Lighty2Config.migrate();
            // save the old config values immediately. the old config file is no longer available and
            // we need to persist them!
            save();
        } else {
            // Try to read the normal config from disk.
            reloadFromDisk();
        }
    }
}

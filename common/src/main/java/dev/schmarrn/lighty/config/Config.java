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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Config {
    private static List<ResourceLocation> stringToRL(String... stuff) {
        return Arrays.stream(stuff).map(ResourceLocation::parse).toList();
    }
    // Internal variables tracking config state
    private static final String PATH = UtilDefinition.INSTANCE.getConfigDir().toString() + "/lighty/base.config";
    private static final Map<String, String> fileState = new HashMap<>();
    private static final Map<String, ConfigSerDe> configValues = new HashMap<>();
    private static boolean initStage = true;

    // all the different config values
    public static final ResourceLocationConfig LAST_USED_RENDERER = new ResourceLocationConfig("lighty.last_used_renderer", ResourceLocation.parse("lighty:renderer_carpet"));
    public static final ResourceLocationListConfig ACTIVE_DATA_PROVIDERS = new ResourceLocationListConfig("lighty.active_data_providers", stringToRL(
            "lighty:data_provider_base",
            "lighty:data_provider_farmland"
    ));

    public static final IntegerConfig SKY_THRESHOLD = new IntegerConfig("lighty.sky_threshold", 0, 0, 15);
    public static final IntegerConfig BLOCK_THRESHOLD = new IntegerConfig("lighty.block_threshold", 0, 0, 15);
    public static final IntegerConfig OVERLAY_DISTANCE = new IntegerConfig("lighty.overlay_distance", 2, 1, 32);
    public static final IntegerConfig OVERLAY_BRIGHTNESS = new IntegerConfig("lighty.overlay_brightness", 10, 0, 15);

    public static final IntegerConfig FARM_GROWTH_THRESHOLD = new IntegerConfig("lighty.farm_growth_threshold", 8, 0, 15);
    public static final IntegerConfig FARM_UPROOT_THRESHOLD = new IntegerConfig("lighty.farm_uproot_threshold", 8, 0, 15);

    public static final BooleanConfig SHOW_SAFE = new BooleanConfig("lighty.show_safe", true);

    public static final ColorConfig OVERLAY_GREEN = new ColorConfig("lighty.overlay_green", 0x00FF00);
    public static final ColorConfig OVERLAY_ORANGE = new ColorConfig("lighty.overlay_orange", 0xFF6600);
    public static final ColorConfig OVERLAY_RED = new ColorConfig("lighty.overlay_red", 0xFF0000);

    public static final ColorConfig ADDITIONAL_OVERLAY_GOOD = new ColorConfig("lighty.additional_overlay_good", 0x00FF66);
    public static final ColorConfig ADDITIONAL_OVERLAY_WARN = new ColorConfig("lighty.additional_overlay_warn", 0x0000FF);
    public static final ColorConfig ADDITIONAL_OVERLAY_BAD = new ColorConfig("lighty.additional_overlay_bad", 0x6600FF);

    public static final ResourceLocationListConfig AUTO_ON_ITEM_LIST = new ResourceLocationListConfig("lighty.auto_on.item", stringToRL(
            "minecraft:torch",
            "minecraft:soul_torch",
            "minecraft:lantern",
            "minecraft:soul_lantern"
    ));
    public static final BooleanConfig SHOULD_AUTO_ON = new BooleanConfig("lighty.auto_on", false);

    public static final BooleanConfig SHOW_SKYLIGHT_LEVEL = new BooleanConfig("lighty.show_skylight_level", true);

    public static final ResourceLocationConfig CARPET_TEXTURE = new ResourceLocationConfig("lighty.mode.carpet.texture", ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "textures/block/transparent.png"));
    public static final ResourceLocationConfig CROSS_TEXTURE = new ResourceLocationConfig("lighty.mode.cross.texture", ResourceLocation.fromNamespaceAndPath(Lighty.MOD_ID, "textures/block/cross.png"));

    private static void loadFromFile(String key, ConfigSerDe type) {
        // If the file contains the config value, get the configured value...
        String value = fileState.getOrDefault(key, null);
        if (value != null) {
            // ... and update the internal value to reflect the file content
            type.deserialize(value);
        }
    }

    public static void register(String key, ConfigSerDe type) {
        configValues.put(key, type);
        loadFromFile(key, type);
    }

    public static void reloadFromDisk() {
        File file = new File(PATH);
        try {
            BufferedReader bf = new BufferedReader(new FileReader(file));

            // For line numbers, I apparently need an atomic integer
            AtomicInteger lineNumber = new AtomicInteger(0);
            bf.lines().forEach((line) -> {
                int ln = lineNumber.incrementAndGet();
                var splits = line.split("=");
                if (splits.length == 2) {
                    fileState.putIfAbsent(splits[0].trim(), splits[1].trim());
                } else {
                    Lighty.LOGGER.warn("Too many = signs on line {}. Removing \"{}\" from config.", ln, line);
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
            loadFromFile(entry.getKey(), entry.getValue());
        }
    }

    public static void save() {
        // If we are in the init stage, do not save to disk
        if (initStage) {
            return;
        }
        StringBuilder content = new StringBuilder();

        for (var pair : configValues.entrySet()) {
            content.append(pair.getKey()).append("=").append(pair.getValue().serialize()).append("\n");
        }

        File file = new File(PATH);
        // Create the lighty config folder if it doesn't already exist
        //noinspection ResultOfMethodCallIgnored (If it doesn't work we'll know about it when the bufferedwriter fails)
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
        // Load config data (first try old config file)
        if (Lighty2Config.exists()) {
            Lighty2Config.migrate();
        } else {
            // If there is no old config, try to read the normal config from disk.
            reloadFromDisk();
        }
        // Init Stage complete!
        initStage = false;
        // save the newly loaded config values to disk once, for good measure (and to persist old config state)
        save();
    }
}

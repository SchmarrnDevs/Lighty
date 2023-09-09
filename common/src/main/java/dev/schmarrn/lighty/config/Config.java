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
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String PATH = UtilDefinition.INSTANCE.getConfigDir().toString() + "/lighty.config";

    private final Properties properties;

    private static Config config;

    private static final String LAST_USED_MODE = "lighty.last_used_mode";
    private static final String SKY_THRESHOLD = "lighty.sky_threshold";
    private static final String BLOCK_THRESHOLD = "lighty.block_threshold";
    private static final String OVERLAY_DISTANCE = "lighty.overlay_distance";
    private static final String OVERLAY_BRIGHTNESS = "lighty.overlay_brightness";
    private static final String SHOW_SAFE = "lighty.show_safe";

    private Config() {
        properties = new Properties();

        try (Reader reader = new FileReader(PATH)) {
            properties.load(reader);

            // Set Defaults if no values are set
            properties.putIfAbsent(LAST_USED_MODE, "lighty:carpet_mode");
            properties.putIfAbsent(SKY_THRESHOLD, "0");
            properties.putIfAbsent(BLOCK_THRESHOLD, "0");
            properties.putIfAbsent(OVERLAY_DISTANCE, "2");
            properties.putIfAbsent(OVERLAY_BRIGHTNESS, "10");
            properties.putIfAbsent(SHOW_SAFE, String.valueOf(true));
        } catch (FileNotFoundException e) {
            Lighty.LOGGER.warn("No Lighty config found at {}, loading defaults and saving config file.", PATH);

            // Add Defaults here
            properties.setProperty(LAST_USED_MODE, "lighty:carpet_mode");
            properties.setProperty(SKY_THRESHOLD, "0");
            properties.setProperty(BLOCK_THRESHOLD, "0");
            properties.setProperty(OVERLAY_DISTANCE, "2");
            properties.setProperty(OVERLAY_BRIGHTNESS, "10");
            properties.setProperty(SHOW_SAFE, String.valueOf(true));


            this.write();
        } catch (IOException e) {
            Lighty.LOGGER.error("Error while reading from Lighty config at {}: {}", PATH, e);
        }
    }

    private void write() {
        try (FileWriter writer = new FileWriter(PATH)){
            properties.store(writer, null);
        } catch (IOException e) {
            Lighty.LOGGER.error("Error while writing to Lighty config at {}: {}", PATH, e);
        }
    }

    public static int getSkyThreshold() {
        return Integer.parseInt(config.properties.getProperty(SKY_THRESHOLD, "0"));
    }

    public static int getBlockThreshold() {
        return Integer.parseInt(config.properties.getProperty(BLOCK_THRESHOLD, "0"));
    }

    public static int getOverlayDistance() {
        return Integer.parseInt(config.properties.getProperty(OVERLAY_DISTANCE, "2"));
    }

    public static int getOverlayBrightness() {
        return Integer.parseInt(config.properties.getProperty(OVERLAY_BRIGHTNESS, "10"));
    }
    public static boolean getShowSafe() {
        return Boolean.parseBoolean(config.properties.getProperty(SHOW_SAFE, String.valueOf(true)));
    }

    public static ResourceLocation getLastUsedMode() {
        return new ResourceLocation(config.properties.getProperty(LAST_USED_MODE, "lighty:carpet_mode"));
    }

    public static void setLastUsedMode(ResourceLocation id) {
        config.properties.setProperty(LAST_USED_MODE, id.toString());
        config.write();
    }

    public static void setSkyThreshold(int i) {
        config.properties.setProperty(SKY_THRESHOLD, String.valueOf(i));
        config.write();
    }

    public static void setBlockThreshold(int i) {
        config.properties.setProperty(BLOCK_THRESHOLD, String.valueOf(i));
        config.write();
    }

    public static void setOverlayDistance(int i) {
        config.properties.setProperty(OVERLAY_DISTANCE, String.valueOf(i));
        config.write();
    }

    public static void setOverlayBrightness(int i) {
        config.properties.setProperty(OVERLAY_BRIGHTNESS, String.valueOf(i));
        config.write();
    }

    public static void setShowSafe(boolean b) {
        config.properties.setProperty(SHOW_SAFE, String.valueOf(b));
        config.write();
    }

    public static void init() {
        config = new Config();
    }
}

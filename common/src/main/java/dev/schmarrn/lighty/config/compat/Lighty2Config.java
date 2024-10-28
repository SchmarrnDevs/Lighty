package dev.schmarrn.lighty.config.compat;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.config.Config;

import java.io.*;
import java.util.Properties;

public class Lighty2Config {
    private static final String PATH = UtilDefinition.INSTANCE.getConfigDir().toString() + "/lighty.config";

    private static final String LAST_USED_MODE = "lighty.last_used_mode";
    private static final String SKY_THRESHOLD = "lighty.sky_threshold";
    private static final String BLOCK_THRESHOLD = "lighty.block_threshold";
    private static final String FARM_GROWTH_THRESHOLD = "lighty.farm_growth_threshold";
    private static final String FARM_UPROOT_THRESHOLD = "lighty.farm_uproot_threshold";
    private static final String OVERLAY_DISTANCE = "lighty.overlay_distance";
    private static final String OVERLAY_BRIGHTNESS = "lighty.overlay_brightness";
    private static final String SHOW_SAFE = "lighty.show_safe";

    private static final String OVERLAY_GREEN = "lighty.overlay_green";
    private static final String OVERLAY_ORANGE = "lighty.overlay_orange";
    private static final String OVERLAY_RED = "lighty.overlay_red";

    public static boolean exists() {
        return new File(PATH).exists();
    }

    /// Migrates the old Lighty Config to the new System.
    public static void migrate() {
        Properties properties = new Properties();

        try (Reader reader = new FileReader(PATH)) {
            properties.load(reader);

            // Set old Defaults if no values are set
            properties.putIfAbsent(LAST_USED_MODE, "lighty:carpet_mode");
            properties.putIfAbsent(SKY_THRESHOLD, "0");
            properties.putIfAbsent(BLOCK_THRESHOLD, "0");
            properties.putIfAbsent(FARM_GROWTH_THRESHOLD, "8");
            properties.putIfAbsent(FARM_UPROOT_THRESHOLD, "8");
            properties.putIfAbsent(OVERLAY_DISTANCE, "2");
            properties.putIfAbsent(OVERLAY_BRIGHTNESS, "10");
            properties.putIfAbsent(SHOW_SAFE, String.valueOf(true));
            properties.putIfAbsent(OVERLAY_GREEN, Integer.toHexString(0x00FF00));
            properties.putIfAbsent(OVERLAY_ORANGE, Integer.toHexString(0xFF6600));
            properties.putIfAbsent(OVERLAY_RED, Integer.toHexString(0xFF0000));

            // Set the new stuff
            Config.SKY_THRESHOLD.setValue(Integer.valueOf(properties.getProperty(SKY_THRESHOLD)));
            Config.BLOCK_THRESHOLD.setValue(Integer.valueOf(properties.getProperty(BLOCK_THRESHOLD)));
            Config.FARM_GROWTH_THRESHOLD.setValue(Integer.valueOf(properties.getProperty(FARM_GROWTH_THRESHOLD)));
            Config.FARM_UPROOT_THRESHOLD.setValue(Integer.valueOf(properties.getProperty(FARM_UPROOT_THRESHOLD)));
            Config.OVERLAY_DISTANCE.setValue(Integer.valueOf(properties.getProperty(OVERLAY_DISTANCE)));
            Config.OVERLAY_BRIGHTNESS.setValue(Integer.valueOf(properties.getProperty(OVERLAY_BRIGHTNESS)));
            Config.SHOW_SAFE.setValue(Boolean.valueOf(properties.getProperty(SHOW_SAFE)));
            Config.OVERLAY_GREEN.setValue(Integer.valueOf(properties.getProperty(OVERLAY_GREEN), 16));
            Config.OVERLAY_ORANGE.setValue(Integer.valueOf(properties.getProperty(OVERLAY_ORANGE), 16));
            Config.OVERLAY_RED.setValue(Integer.valueOf(properties.getProperty(OVERLAY_RED), 16));
        } catch (FileNotFoundException e) {
            Lighty.LOGGER.warn("No Lighty config found at {}, loading defaults and saving config file.", PATH);
        } catch (IOException e) {
            Lighty.LOGGER.error("Error while reading from Lighty config at {}: {}", PATH, e);
        }

        new File(PATH).delete();
    }
}

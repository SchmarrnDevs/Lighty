package dev.schmarrn.lighty.config;

import dev.schmarrn.lighty.Lighty;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String PATH = FabricLoader.getInstance().getConfigDir().toString() + "/lighty.config";

    private final Properties properties;

    private static Config config;

    private static final String LAST_USED_MODE = "lighty.last_used_mode";
    private static final String SKY_THRESHOLD = "lighty.sky_threshold";
    private static final String BLOCK_THRESHOLD = "lighty.block_threshold";

    private Config() {
        properties = new Properties();

        try (Reader reader = new FileReader(PATH)) {
            properties.load(reader);

            // Set Defaults if no values are set
            properties.putIfAbsent(LAST_USED_MODE, "lighty:carpet_mode");
            properties.putIfAbsent(SKY_THRESHOLD, "0");
            properties.putIfAbsent(BLOCK_THRESHOLD, "0");
        } catch (FileNotFoundException e) {
            Lighty.LOGGER.warn("No Lighty config found at {}, loading defaults and saving config file.", PATH);

            // Add Defaults here
            properties.setProperty(LAST_USED_MODE, "lighty:carpet_mode");
            properties.setProperty(SKY_THRESHOLD, "0");
            properties.setProperty(BLOCK_THRESHOLD, "0");

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

    public static Identifier getLastUsedMode() {
        return new Identifier(config.properties.getProperty(LAST_USED_MODE, "lighty:carpet_mode"));
    }

    public static int getSkyThreshold() {
        return Integer.parseInt(config.properties.getProperty(SKY_THRESHOLD, "0"));
    }

    public static int getBlockThreshold() {
        return Integer.parseInt(config.properties.getProperty(BLOCK_THRESHOLD, "0"));
    }

    public static void setLastUsedMode(Identifier id) {
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

    public static void init() {
        config = new Config();
    }
}

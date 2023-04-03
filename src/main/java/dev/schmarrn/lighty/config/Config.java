package dev.schmarrn.lighty.config;

import dev.schmarrn.lighty.Lighty;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String PATH = FabricLoader.getInstance().getConfigDir().toString() + "/lighty.config";

    private final Properties properties;

    private static Config config;

    private static final String LAST_USED_MODE = "lighty.last_used_mode";

    private Config() {
        properties = new Properties();

        try (Reader reader = new FileReader(PATH)) {
            properties.load(reader);
        } catch (FileNotFoundException e) {
            Lighty.LOGGER.warn("No Lighty config found at {}, loading defaults.", PATH);

            // Add Defaults here
            properties.setProperty(LAST_USED_MODE, "lighty:carpet_mode");

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

    public static ResourceLocation getLastUsedMode() {
        return new ResourceLocation(config.properties.getProperty(LAST_USED_MODE));
    }

    public static void setLastUsedMode(ResourceLocation id) {
        config.properties.setProperty(LAST_USED_MODE, id.toString());
        config.write();
    }

    public static void init() {
        config = new Config();
    }
}

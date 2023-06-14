package dev.schmarrn.lighty;

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.mode.BoringCrossMode;
import dev.schmarrn.lighty.mode.CarpetMode;
import dev.schmarrn.lighty.mode.NumberMode;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lighty {

    public static final String MOD_ID = "lighty";
    public static final String MOD_NAME = "Lighty";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("Let there be {}", MOD_NAME);

        Config.init();
        CarpetMode.init();
        NumberMode.init();
        BoringCrossMode.init();
        KeyBind.init();
    }

    public static void postLoad() {
        ModeLoader.setLastUsedMode();
    }
}

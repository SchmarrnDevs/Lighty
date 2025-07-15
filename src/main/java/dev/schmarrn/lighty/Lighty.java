package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyModesRegistration;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import dev.schmarrn.lighty.event.Render;
import dev.schmarrn.lighty.mode.CarpetMode;
import dev.schmarrn.lighty.mode.CrossMode;
import dev.schmarrn.lighty.mode.NumberMode;
import dev.schmarrn.lighty.ui.LightyConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;


public class Lighty implements ClientModInitializer, ClientStartEntrypoint {
    public static final String MOD_ID = "lighty";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Let there be {}", MOD_ID);

		Config.init();

		KeyBind.init();
		Compute.init();
		Render.init();

		CarpetMode.init();
		CrossMode.init();
		NumberMode.init();
		FabricLoader.getInstance().getEntrypoints("lightyModesRegistration", LightyModesRegistration.class).forEach(LightyModesRegistration::registerLightyModes);

		ModeLoader.setLastUsedMode();
	}

	@Override
	public void beforeClientStart() { }

	@Override
	public void afterClientStart() {
		KeyBind.register();
		LightyConfigScreen.register();
	}
}

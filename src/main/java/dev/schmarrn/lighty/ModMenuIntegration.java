package dev.schmarrn.lighty;

import dev.schmarrn.lighty.ui.LightyConfigScreen;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.Screen;

import java.util.function.Function;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public String getModId() {
		return Lighty.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return LightyConfigScreen::new;
	}
}

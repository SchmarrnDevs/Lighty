package dev.schmarrn.lighty.event;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.SMACH;
import dev.schmarrn.lighty.api.LightyMode;

public class Render {
	public static void renderOverlay(float partialTicks) {
		if (!SMACH.isEnabled()) return;

		LightyMode<?, ?> mode = ModeLoader.getCurrentMode();
		assert mode != null;

		mode.render(partialTicks);
	}

	public static void init() {

	}
}

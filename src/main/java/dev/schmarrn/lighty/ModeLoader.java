package dev.schmarrn.lighty;

import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModeLoader {
	@Nullable
	private static LightyMode<?, ?> mode = null;

	private static final HashMap<String, LightyMode<?, ?>> MODES = new HashMap<>();

	public static void loadMode(String id) {
		LightyMode<?, ?> modeToLoad = MODES.get(id);

		if (modeToLoad == null) {
			Lighty.LOGGER.error("Trying to load unregistered mode with id {}! Not changing mode.", id);
			return;
		}

		if (ModeLoader.mode != null) {
			ModeLoader.mode.unload();
		}

		ModeLoader.mode = modeToLoad;
		Config.LAST_USED_RENDERER.setValue(id);
		Compute.markDirty();

		SMACH.enable();
	}

	public static void clear() {
		MODES.forEach((id, mode) -> {
			mode.unload();
		});
	}

	public static void put(String id, LightyMode<?, ?> mode) {
		MODES.put(id, mode);
	}

	@Nullable
	public static LightyMode<?, ?> getCurrentMode() {
		if (!SMACH.isEnabled()) return null;
		return mode;
	}

	/**
	 * Needs to be called AFTER registering all the different Lighty modes.
	 * If the requested mode isn't loaded, default to the first registered mode.
	 */
	public static void setLastUsedMode() {
		mode = MODES.getOrDefault(Config.LAST_USED_RENDERER.getValue(), MODES.values().iterator().next());
	}

	private ModeLoader() {}
}

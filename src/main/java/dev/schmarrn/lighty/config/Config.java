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
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Config {
	// Internal variables tracking config state
	private static final String PATH = FabricLoader.getInstance().getConfigDir().toString() + "/"+Lighty.MOD_ID+"/base.config";
	private static final Map<String, String> fileState = new HashMap<>();
	private static final Map<String, ConfigSerDe> configValues = new HashMap<>();
	private static boolean initStage = true;

	// all the different config values
	public static final StringConfig LAST_USED_RENDERER = new StringConfig("lighty.last_used_renderer", "lighty:renderer_carpet");

	public static final IntegerConfig SKY_THRESHOLD = new IntegerConfig("lighty.sky_threshold", 0, 0, 15);
	public static final IntegerConfig BLOCK_THRESHOLD = new IntegerConfig("lighty.block_threshold", 0, 0, 15);
	public static final IntegerConfig OVERLAY_DISTANCE = new IntegerConfig("lighty.overlay_distance", 2, 1, 32);
	public static final IntegerConfig OVERLAY_BRIGHTNESS = new IntegerConfig("lighty.overlay_brightness", 15, 0, 15);
	public static final IntegerConfig OVERLAY_TRANSPARENCY = new IntegerConfig("lighty.overlay_transparency", 60, 0, 100);
	public static final IntegerConfig OVERLAY_LINE_THICKNESS = new IntegerConfig("lighty.overlay_line_thickness", 2, 0, 15);

	public static final BooleanConfig SHOW_SAFE = new BooleanConfig("lighty.show_safe", true);
	public static final BooleanConfig FLAT_CARPET = new BooleanConfig("lighty.flat_carpet", false);

	public static final ColorConfig OVERLAY_GREEN = new ColorConfig("lighty.overlay_green", 0x00FF00);
	public static final ColorConfig OVERLAY_ORANGE = new ColorConfig("lighty.overlay_orange", 0xFF6600);
	public static final ColorConfig OVERLAY_RED = new ColorConfig("lighty.overlay_red", 0xFF0000);

	public static final StringListConfig AUTO_ON_ITEM_LIST = new StringListConfig("lighty.auto_on.item", Arrays.asList(
		"minecraft:block/torch_coal",
		"minecraft:item/lantern_firefly_green",
		"minecraft:item/lantern_firefly_blue",
		"minecraft:item/lantern_firefly_orange",
		"minecraft:item/lantern_firefly_red"
	));
	public static final BooleanConfig SHOULD_AUTO_ON = new BooleanConfig("lighty.auto_on", false);
	public static final BooleanConfig SHOW_SKYLIGHT_LEVEL = new BooleanConfig("lighty.show_skylight_level", true);
	public static final BooleanConfig SHOW_ABOVE_HITBOX = new BooleanConfig("lighty.show_above_hitbox", true);

	public static final StringConfig CARPET_TEXTURE = new StringConfig("lighty.mode.carpet.texture", "/assets/lighty/textures/block/transparent.png");

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
				String[] splits = line.split("=");
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

		for (Map.Entry<String, ConfigSerDe> entry : configValues.entrySet()) {
			loadFromFile(entry.getKey(), entry.getValue());
		}
	}

	public static void save() {
		// If we are in the init stage, do not save to disk
		if (initStage) {
			return;
		}
		StringBuilder content = new StringBuilder();

		for (Map.Entry<String, ConfigSerDe> pair : configValues.entrySet()) {
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
		/* If there is no old config, try to read the normal config from disk. */
		reloadFromDisk();

		// Init Stage complete!
		initStage = false;
		// save the newly loaded config values to disk once, for good measure (and to persist old config state)
		save();
	}
}

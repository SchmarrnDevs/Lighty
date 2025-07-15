package dev.schmarrn.lighty.config;

import net.minecraft.client.gui.options.components.OptionsComponent;

public class ColorConfig extends ConfigType<Integer> {
	@Override
	public OptionsComponent getOptionInstance() {
		return null;
	}

	public ColorConfig(String key, Integer defaultValue) {
		super(key, defaultValue);
	}

	@Override
	String serialize() {
		int color = getValue();
		StringBuilder ret = new StringBuilder("0x");
		for (int ii = 5; ii >= 0; --ii) {
			int nibble = (color & (0xF << (4*ii))) >> (4*ii);
			ret.append(Integer.toHexString(nibble));
		}
		return ret.toString();
	}

	@Override
	void deserialize(String color) {
		setValue(Integer.parseUnsignedInt(color.replace("0x", ""), 16));
	}
}

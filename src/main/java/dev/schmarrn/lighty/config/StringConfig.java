package dev.schmarrn.lighty.config;

import net.minecraft.client.gui.options.components.OptionsComponent;

public class StringConfig extends ConfigType<String> {
	@Override
	public OptionsComponent getOptionInstance() {
		return null;
	}

	public StringConfig(String key, String defaultValue) {
		super(key, defaultValue);
	}

	@Override
	String serialize() {
		return getValue();
	}

	@Override
	void deserialize(String value) {
		setValue(value);
	}
}

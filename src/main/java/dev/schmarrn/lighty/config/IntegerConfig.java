package dev.schmarrn.lighty.config;

import dev.schmarrn.lighty.Lighty;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.option.OptionRange;

public class IntegerConfig extends ConfigType<Integer> {
	private final int min, max;
	@Override
	public ButtonComponent getOptionInstance() {
		return new ToggleableOptionComponent<>(new OptionRange(null, this.getTranslationKey(), getValue(), max+1) {
			@Override
			public void onUpdate() {
				setValue(this.value);
			}

			@Override
			public String getDisplayString() {
				return  getDisplayStringValue();
			}

			@Override
			public String getDisplayStringValue() {
				return  getValue()+"";
			}
		});
	}

	public IntegerConfig(String key, Integer defaultValue, int min, int max) {
		super(key, defaultValue);
		this.min = min;
		this.max = max;
	}

	public int getMax() {
		return this.max;
	}

	public int getMin() {
		return this.min;
	}

	@Override
	public void setValue(Integer newValue) {
		if (newValue <= max) {
			super.setValue(Math.max(min, newValue));
		} else {
			Lighty.LOGGER.error("Lighty Config: {}, new value {} out of bounds: [{}, {}]. Ignoring new value.", getKey(), newValue, min, max);
		}
	}

	@Override
	String serialize() {
		return Integer.toString(getValue());
	}

	@Override
	void deserialize(String value) {
		setValue(Integer.valueOf(value));
	}
}

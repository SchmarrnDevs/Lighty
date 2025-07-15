package dev.schmarrn.lighty.config;

import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.ButtonComponent;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.core.lang.I18n;

public class BooleanConfig extends ConfigType<Boolean> {
	@Override
	public ButtonComponent getOptionInstance() {
		return new BooleanOptionComponent(
			new OptionBoolean(null, this.getTranslationKey(), this.getValue()) {
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
					I18n i18n = I18n.getInstance();
					return  i18n.translateKey(getValue() ? "options.on" : "options.off");
				}
			}
		);
	}

	public BooleanConfig(String key, Boolean defaultValue) {
		super(key, defaultValue);
	}

	@Override
	String serialize() {
		return Boolean.toString(getValue());
	}

	@Override
	void deserialize(String value) {
		setValue(Boolean.valueOf(value));
	}
}

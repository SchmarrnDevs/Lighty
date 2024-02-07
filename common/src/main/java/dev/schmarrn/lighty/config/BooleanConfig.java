package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class BooleanConfig extends ConfigType<Boolean> {
    @Override
    public OptionInstance<Boolean> getOptionInstance() {
        return OptionInstance.createBoolean(
                this.getTranslationKey(),
                object -> Tooltip.create(Component.translatable(this.getTranslationTooltipKey(), object)),
                this.getValue(),
                this::setValue
        );
    }

    BooleanConfig(String key, Boolean defaultValue) {
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

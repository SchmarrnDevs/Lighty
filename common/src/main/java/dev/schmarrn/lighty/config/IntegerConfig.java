package dev.schmarrn.lighty.config;

import com.mojang.serialization.Codec;
import dev.schmarrn.lighty.Lighty;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class IntegerConfig extends ConfigType<Integer> {
    private final int min, max;
    @Override
    public OptionInstance<Integer> getOptionInstance() {
        return new OptionInstance<>(
                this.getTranslationKey(),
                object -> Tooltip.create(Component.translatable(this.getTranslationTooltipKey(), object)),
                (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                new OptionInstance.IntRange(this.min, this.max),
                Codec.intRange(this.min, this.max),
                this.getValue(),
                this::setValue
        );
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
        if (newValue >= min && newValue <= max) {
            super.setValue(newValue);
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

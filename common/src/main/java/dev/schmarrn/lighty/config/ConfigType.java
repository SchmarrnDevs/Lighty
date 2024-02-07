package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;

abstract class ConfigType<T> extends ConfigSerDe{
    public void onChange(T value){}

    private final T DEFAULT_VALUE;
    private final String KEY;

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        value = newValue;
        onChange(newValue);
        Config.save();
    }

    public void resetToDefault() {
        value = DEFAULT_VALUE;
    }

    public String getKey() {
        return KEY;
    }

    public abstract OptionInstance<T> getOptionInstance();
    protected String getTranslationKey() {
        return KEY.replaceFirst("\\.", ".options.");
    }

    protected String getTranslationTooltipKey() {
        return getTranslationKey() + ".tooltip";
    }

    ConfigType(String key, T defaultValue) {
        this.KEY = key;
        this.DEFAULT_VALUE = defaultValue;
        this.value = defaultValue;

        Config.register(key, this);
    }
}

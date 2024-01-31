package dev.schmarrn.lighty.config;

public class BooleanConfig extends ConfigType<Boolean> {
    BooleanConfig(String key, Boolean defaultValue) {
        super(key, defaultValue);

        Config.register(key, this);
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

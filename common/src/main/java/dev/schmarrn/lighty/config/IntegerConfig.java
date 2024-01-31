package dev.schmarrn.lighty.config;

public class IntegerConfig extends ConfigType<Integer> {
    IntegerConfig(String key, Integer defaultValue) {
        super(key, defaultValue);

        Config.register(key, this);
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

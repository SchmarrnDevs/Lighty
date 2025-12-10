package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.resources.Identifier;

public class IdentifierConfig extends ConfigType<Identifier> {
    @Override
    public OptionInstance<Identifier> getOptionInstance() {
        return null;
    }

    public IdentifierConfig(String key, Identifier defaultValue) {
        super(key, defaultValue);
    }

    @Override
    String serialize() {
        return getValue().toString();
    }

    @Override
    void deserialize(String value) {
        setValue(Identifier.parse(value));
    }
}

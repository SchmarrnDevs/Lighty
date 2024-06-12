package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationConfig extends ConfigType<ResourceLocation> {
    @Override
    public OptionInstance<ResourceLocation> getOptionInstance() {
        return null;
    }

    public ResourceLocationConfig(String key, ResourceLocation defaultValue) {
        super(key, defaultValue);
    }

    @Override
    String serialize() {
        return getValue().toString();
    }

    @Override
    void deserialize(String value) {
        setValue(ResourceLocation.parse(value));
    }
}

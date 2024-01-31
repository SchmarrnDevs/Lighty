package dev.schmarrn.lighty.config;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationConfig extends ConfigType<ResourceLocation> {
    ResourceLocationConfig(String key, ResourceLocation defaultValue) {
        super(key, defaultValue);

        Config.register(key, this);
    }

    @Override
    String serialize() {
        return getValue().toString();
    }

    @Override
    void deserialize(String value) {
        setValue(new ResourceLocation(value));
    }
}

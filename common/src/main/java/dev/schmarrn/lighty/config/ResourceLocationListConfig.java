package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class ResourceLocationListConfig extends ConfigType<List<ResourceLocation>> {
    public ResourceLocationListConfig(String key, List<ResourceLocation> defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public OptionInstance<List<ResourceLocation>> getOptionInstance() {
        return null;
    }

    @Override
    String serialize() {
        StringBuilder builder = new StringBuilder();
        for (int ii = 0; ii < this.getValue().size() - 1; ii++) {
            builder.append(this.getValue().get(ii).toString());
            builder.append(" ");
        }
        builder.append(this.getValue().getLast());
        return builder.toString();
    }

    @Override
    void deserialize(String value) {
        setValue(Arrays.stream(value.split(" ")).map(ResourceLocation::parse).toList());
    }
}

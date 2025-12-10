package dev.schmarrn.lighty.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.List;

public class IdentifierListConfig extends ConfigType<List<Identifier>> {
    public IdentifierListConfig(String key, List<Identifier> defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public OptionInstance<List<Identifier>> getOptionInstance() {
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
        setValue(Arrays.stream(value.split(" ")).map(Identifier::parse).toList());
    }
}

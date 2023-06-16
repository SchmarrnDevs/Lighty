package dev.schmarrn.lighty.fabric;

import dev.schmarrn.lighty.UtilDefinition;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.model.BakedModel;

import java.nio.file.Path;

public class UtilFabricImpl implements UtilDefinition {

    @Override
    public KeyMapping registerKeyBinding(KeyMapping mapping) {
        return KeyBindingHelper.registerKeyBinding(mapping);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    //@Override
    public BakedModel getCarpetModel() {
        return null;
    }
}

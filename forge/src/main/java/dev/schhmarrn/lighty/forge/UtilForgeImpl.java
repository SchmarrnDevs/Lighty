package dev.schhmarrn.lighty.forge;

import dev.schmarrn.lighty.UtilDefinition;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UtilForgeImpl implements UtilDefinition {

    private static final List<KeyMapping> MAPPINGS = new ArrayList<>();

    @Override
    public KeyMapping registerKeyBinding(KeyMapping mapping) {
        MAPPINGS.add(mapping);
        return mapping;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        MAPPINGS.forEach(event::register);
    }
}

package dev.schmarrn.lighty.forge;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.compat.IrisCompat;
import dev.schmarrn.lighty.core.Compute;
import dev.schmarrn.lighty.core.LightyPipelines;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.neoforged.fml.ModList;

import java.util.Objects;

public class IrisCompatForgeImpl implements IrisCompat {
    private static final String SODIUM_ID = "sodium";
    private static final String IRIS_ID = "iris";

    private static boolean isSodiumLoaded = false;
    private static boolean isIrisLoaded = false;
    private static String sodiumVersion = "";
    private static String irisVersion = "";

    private static IrisApi irisAPI = null;

    static {
        ModList.get().getMods().stream().toList().forEach((modInfo -> {
            if (Objects.equals(modInfo.getModId(), SODIUM_ID)) {
                sodiumVersion = modInfo.getVersion().toString();
                isSodiumLoaded = true;
            }
            else if (Objects.equals(modInfo.getModId(), IRIS_ID)) {
                irisVersion = modInfo.getVersion().toString();
                isIrisLoaded = true;
                irisAPI = IrisApi.getInstance();
            }
        }));
    }

    private boolean wereShadersOn = shadersEnabled();

    @Override
    public void fixIrisShaders() {
        if (wereShadersOn ^ shadersEnabled()) {
            // If shaders were switched on/off, we need to re-compute the overlay
            Lighty.LOGGER.info("Iris shader state changed, need to re-compute Lighty overlay.");
            wereShadersOn = shadersEnabled();
            Compute.clear();
        }
    }

    @Override
    public boolean hasIris() {
        return isSodiumLoaded && isIrisLoaded;
    }

    @Override
    public void registerPipelines() {
        // this fix doesn't work in neoforge, idk why
        //irisAPI.assignPipeline(LightyPipelines.TERRAIN_TRANSLUCENT, IrisProgram.TRANSLUCENT);
        //irisAPI.assignPipeline(LightyPipelines.TERRAIN_CUTOUT, IrisProgram.TERRAIN_CUTOUT);
    }

    public boolean shadersEnabled() {
        return irisAPI != null && irisAPI.isShaderPackInUse();
    }
}

// Copyright 2022-2023 The Lighty contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.schmarrn.lighty.fabric;

import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.core.LightyPipelines;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.minecraft.client.KeyMapping;

import java.nio.file.Path;

public class UtilFabricImpl implements UtilDefinition {
    private static final IrisApi IRIS;
    static {
        IrisApi i;
        try {
            i = (IrisApi)Class.forName("net.irisshaders.iris.apiimpl.IrisApiV0Impl").getField("INSTANCE").get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException var1) {
            i = null;
        }
        IRIS = i;
    }

    @Override
    public KeyMapping registerKeyBinding(KeyMapping mapping) {
        return KeyMappingHelper.registerKeyMapping(mapping);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean shadersEnabled() {
        return IRIS != null && IRIS.isShaderPackInUse();
    }

    @Override
    public void registerPipelinesWithIris() {
        if (IRIS != null) {
            IRIS.assignPipeline(LightyPipelines.TERRAIN_TRANSLUCENT, IrisProgram.TRANSLUCENT);
            IRIS.assignPipeline(LightyPipelines.TERRAIN_CUTOUT, IrisProgram.TERRAIN_CUTOUT);
            IRIS.assignPipeline(LightyPipelines.LINES, IrisProgram.LINES);
        }
    }
}

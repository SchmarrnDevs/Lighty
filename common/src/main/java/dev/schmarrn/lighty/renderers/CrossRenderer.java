package dev.schmarrn.lighty.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.schmarrn.lighty.api.OverlayData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class CrossRenderer {
    public void compute(ClientLevel level, BlockPos pos, OverlayData data, BufferBuilder builder, int lightmap) {
        float x1 = pos.getX() % 16;
        float x2 = pos.getX() % 16 + 1f;
        float y = pos.getY() % 16 + 1.005f + data.yOffset();
        float z1 = pos.getZ() % 16;
        float z2 = pos.getZ() % 16 + 1f;

        builder.addVertex(x1, y, z1).setColor(data.color()).setUv(0, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x1, y, z2).setColor(data.color()).setUv(0, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x2, y, z2).setColor(data.color()).setUv(1, 1).setLight(lightmap).setNormal(0f, 1f, 0f);
        builder.addVertex(x2, y, z1).setColor(data.color()).setUv(1, 0).setLight(lightmap).setNormal(0f, 1f, 0f);
    }
}

package dev.schmarrn.lighty.fabric.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

/// Stolen from NeoForge AddSectionGeometryEvent
public interface AddSectionGeometryEvent {
    Event<AddSectionGeometryEvent> EVENT = EventFactory.createArrayBacked(
            AddSectionGeometryEvent.class,
            (listeners) -> (additionalRenderers, sectionOrigin, level) -> {
                for (var listener : listeners) {
                    listener.run(additionalRenderers, sectionOrigin, level);
                }
            });

    void run(List<AdditionalSectionRenderer> additionalRenderers, BlockPos sectionOrigin, Level level);


    @FunctionalInterface
    interface AdditionalSectionRenderer {
        void render(SectionRenderingContext context);
    }

    final class SectionRenderingContext {
        private final Function<ChunkSectionLayer, VertexConsumer> getOrCreateLayer;
        private final BlockAndTintGetter region;
        private final ModelBlockRenderer blockRenderer;

        public SectionRenderingContext(Function<ChunkSectionLayer, VertexConsumer> getOrCreateLayer, BlockAndTintGetter region, ModelBlockRenderer blockRenderer) {
            this.getOrCreateLayer = getOrCreateLayer;
            this.region = region;
            this.blockRenderer = blockRenderer;
        }

        public VertexConsumer getOrCreateChunkBuffer(ChunkSectionLayer layer) {
            return getOrCreateLayer.apply(layer);
        }

        public ModelBlockRenderer getBlockRenderer() {
            return blockRenderer;
        }

        public BlockAndTintGetter getRegion() {
            return region;
        }
    }
}

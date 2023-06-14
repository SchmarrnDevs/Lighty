package dev.schmarrn.lighty.ui;

import com.mojang.serialization.Codec;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsScreen extends Screen {
    public SettingsScreen() {
        super(Component.translatable("settings.lighty.title"));
    }

    OptionsList list;

    @Override
    protected void init() {
        assert this.minecraft != null;
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

        list.addBig(new OptionInstance<>(
                "lighty.options.overlay_distance",
                object -> Tooltip.create(Component.translatable("lighty.options.overlay_distance.tooltip", object)),
                (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                new OptionInstance.IntRange(1, 32),
                Codec.intRange(1, 32),
                Config.getOverlayDistance(),
                Config::setOverlayDistance
        ));

        list.addSmall(
                new OptionInstance<>(
                        "lighty.options.block_threshold",
                        object -> Tooltip.create(Component.translatable("lighty.options.block_threshold.tooltip", object)),
                        (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                        new OptionInstance.IntRange(0, 15),
                        Codec.intRange(0, 15),
                        Config.getBlockThreshold(),
                        Config::setBlockThreshold
                ),
                new OptionInstance<>(
                        "lighty.options.sky_threshold",
                        object -> Tooltip.create(Component.translatable("lighty.options.sky_threshold.tooltip", object)),
                        (component, integer) -> Options.genericValueLabel(component, Component.literal(integer.toString())),
                        new OptionInstance.IntRange(0, 15),
                        Codec.intRange(0, 15),
                        Config.getSkyThreshold(),
                        Config::setSkyThreshold
                )

        );

        this.addWidget(list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        list.render(guiGraphics, i, j , f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        Compute.clear();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

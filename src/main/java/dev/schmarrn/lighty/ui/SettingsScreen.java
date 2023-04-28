package dev.schmarrn.lighty.ui;

import com.mojang.serialization.Codec;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SettingsScreen extends Screen {
    public SettingsScreen() {
        super(Text.translatable("settings.lighty.title"));
    }

    ButtonListWidget list;

    @Override
    protected void init() {
        assert this.client != null;
        this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

        list.addOptionEntry(
                new SimpleOption<>(
                        "lighty.options.block_threshold",
                        SimpleOption.constantTooltip(Text.translatable("lighty.options.block_threshold.tooltip")),
                        (component, integer) -> GameOptions.getGenericValueText(component, Text.literal(integer.toString())),
                        new SimpleOption.ValidatingIntSliderCallbacks(0, 15),
                        Codec.intRange(0, 15),
                        Config.getBlockThreshold(),
                        Config::setBlockThreshold
                ),
                new SimpleOption<>(
                        "lighty.options.sky_threshold",
                        SimpleOption.constantTooltip(Text.translatable("lighty.options.sky_threshold.tooltip")),
                        (component, integer) -> GameOptions.getGenericValueText(component, Text.literal(integer.toString())),
                        new SimpleOption.ValidatingIntSliderCallbacks(0, 15),
                        Codec.intRange(0, 15),
                        Config.getSkyThreshold(),
                        Config::setSkyThreshold
                )
        );

        this.addDrawableChild(list);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.close()));
    }

    @Override
    public void render(@NotNull MatrixStack guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        list.render(guiGraphics, i, j , f);
        drawCenteredTextWithShadow(guiGraphics, this.textRenderer, this.title.asOrderedText(), this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void close() {
        Compute.markDirty();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

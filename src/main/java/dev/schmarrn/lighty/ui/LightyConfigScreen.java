package dev.schmarrn.lighty.ui;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.OptionsComponent;
import net.minecraft.client.gui.options.components.ShortcutComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.util.helper.FileOpener;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LightyConfigScreen extends ScreenOptions {

	private static final List<OptionsPage> OPTION_PAGES = new ArrayList<>();
	private static final List<Supplier<OptionsComponent>> MODE_OPTIONS = new ArrayList<>();

	public LightyConfigScreen(Screen parent) {
		super(parent, getDefaultPage());
	}

	@Override
	public void removed() {
		for (OptionsPage optionPage : OPTION_PAGES)
			for (OptionsComponent component : optionPage.getComponents())
				component.onClose();

		super.removed();
		Config.save();
		Compute.markDirty();
	}

	@Override
	public void mouseReleased(int mx, int my, int buttonNum) {
		super.mouseReleased(mx, my, buttonNum);
		Compute.markDirty();
	}

	public static OptionsPage getDefaultPage() {
		return OPTION_PAGES.get(0);
	}

	public static List<OptionsPage> getPages() {
		return OPTION_PAGES;
	}

	public static void register() {
		OPTION_PAGES.add(new OptionsPage("gui."+Lighty.MOD_ID+".options.general.title", new ItemStack(Blocks.TORCH_COAL))
			.withComponent(new OptionsCategory("gui."+ Lighty.MOD_ID+".options.toggle")
				.withComponent(Config.SHOW_SAFE.getOptionInstance())
				.withComponent(Config.SHOULD_AUTO_ON.getOptionInstance())
			)
			.withComponent(new OptionsCategory("gui."+ Lighty.MOD_ID+".options.visual")
				.withComponent(Config.OVERLAY_DISTANCE.getOptionInstance())
				.withComponent(Config.OVERLAY_BRIGHTNESS.getOptionInstance())
				.withComponent(Config.OVERLAY_TRANSPARENCY.getOptionInstance())
			)
			.withComponent(new OptionsCategory("gui."+ Lighty.MOD_ID+".options.advanced")
				.withComponent(Config.SKY_THRESHOLD.getOptionInstance())
				.withComponent(Config.BLOCK_THRESHOLD.getOptionInstance())
				.withComponent(new ShortcutComponent("gui.lighty.options.page.advanced.button.open_folder", () -> FileOpener.open(new File(FabricLoader.getInstance().getConfigDir().toString(), Lighty.MOD_ID))))
			)
		);

		OptionsPage page = new OptionsPage("gui."+Lighty.MOD_ID+".options.modes.title", new ItemStack(Blocks.BOOKSHELF_PLANKS_OAK));
		for (Supplier<OptionsComponent> component : MODE_OPTIONS) {
			page.withComponent(component.get());
		}
		MODE_OPTIONS.clear();
		OPTION_PAGES.add(page);
	}

	@SafeVarargs
	public static void addModeOption(Supplier<OptionsComponent>... components) {
		Collections.addAll(MODE_OPTIONS, components);
	}
}

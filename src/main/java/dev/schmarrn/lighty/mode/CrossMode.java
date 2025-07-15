package dev.schmarrn.lighty.mode;

import dev.schmarrn.lighty.Lighty;
import dev.schmarrn.lighty.api.LightyMode;
import dev.schmarrn.lighty.api.ModeManager;
import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.util.collection.Pair;
import org.lwjgl.opengl.GL11;

public class CrossMode extends LightyMode<Provider.Pos, Pair<Double, Integer>> {

	public static void init() {
		ModeManager.registerMode(Lighty.MOD_ID+".cross_mode", new CrossMode());
		ModeManager.addOptions(
				() -> new OptionsCategory("gui.modeSwitcher."+Lighty.MOD_ID+".cross_mode")
						.withComponent(Config.OVERLAY_LINE_THICKNESS.getOptionInstance())
		);
	}

	@Override
	public void compute(WorldClient world, int x, int y, int z) {
		if (Provider.isBlocked(x, y+1, z, world)) return;

		Pair<Integer, Integer> light = Provider.compute(world, x, y, z);
		if (light == null) return;

		Integer color = Provider.getColor(light);
		if (color == null) return;

		double offset = 0;

		Block<?> block = world.getBlock(x, y+1, z);
		if (block != null && block.id() == Blocks.LAYER_SNOW.id())
			offset += block.getBlockBoundsFromState(world, x, y+1, z).maxY;

		cache.put(new Provider.Pos(x, y+1, z), Pair.of(offset, color));
	}

	@Override
	public void render(float partialTicks) {
		Minecraft minecraft = Minecraft.getMinecraft();
		ICamera camera = minecraft.activeCamera;
		if (camera == null) return;

		GL11.glPushMatrix();
		if (Config.OVERLAY_TRANSPARENCY.getValue() < 100)
			GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		if (LightmapHelper.isLightmapEnabled()) {
			Integer light = Config.OVERLAY_BRIGHTNESS.getValue();
			LightmapHelper.setLightmapCoord(light, light);
		}

		cache.forEach((pos, data) -> {
			double x = pos.x + 0.44 - camera.getX(partialTicks);
			double y = pos.y + data.getLeft() + 0.01 - camera.getY(partialTicks);
			double z = pos.z + 0.562 - camera.getZ(partialTicks);

			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glRotated(90, 1, 0, 0);
			GL11.glRotated(45, 0, 0, 1);
			GL11.glScalef(2.85f/32f, -2.85f/32f, 2.85f/32f);

			drawCross(data.getRight());

			GL11.glPopMatrix();
		});

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if (Config.OVERLAY_TRANSPARENCY.getValue() < 100)
			GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private static void drawCross(int color) {
		GL11.glLineWidth(Config.OVERLAY_LINE_THICKNESS.getValue());

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.setColorRGBA(
			(color >> 16) & 0xFF,
			(color >> 8) & 0xFF,
			color & 0xFF,
			(int) (2.55 * Config.OVERLAY_TRANSPARENCY.getValue())
		);

		tessellator.addVertex(-8, 1, 0);
		tessellator.addVertex(+8, 1, 0);

		tessellator.addVertex(0, 1 - 8, 0);
		tessellator.addVertex(0, 1 + 8, 0);

		tessellator.draw();
	}
}

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
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.phys.AABB;
import org.lwjgl.opengl.GL11;

public class NumberMode extends LightyMode<Provider.Pos, NumberMode.Data> {

	private static final float TEXTURE_SIZE = 32;

	public static void init() {
		ModeManager.registerMode(Lighty.MOD_ID+".number_mode", new NumberMode());
		ModeManager.addOptions(
				() -> new OptionsCategory("gui.modeSwitcher."+Lighty.MOD_ID+".number_mode")
						.withComponent(Config.SHOW_SKYLIGHT_LEVEL.getOptionInstance())
						.withComponent(Config.SHOW_ABOVE_HITBOX.getOptionInstance())
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

		Block<?> block = world.getBlock(x, y + 1, z);
		if (block != null) {
			AABB bounds = block.getBlockBoundsFromState(world, x, y + 1, z);
			offset += bounds.maxY;
		}

		cache.put(new Provider.Pos(x, y+1, z), new Data(light.getLeft(), light.getRight(), offset, color));
	}

	@Override
	public void render(float partialTicks) {
		Minecraft minecraft = Minecraft.getMinecraft();
		ICamera camera = minecraft.activeCamera;
		if (camera == null) return;

		GL11.glPushMatrix();
		if (Config.OVERLAY_TRANSPARENCY.getValue() < 100)
			GL11.glEnable(GL11.GL_BLEND);

		if (LightmapHelper.isLightmapEnabled()) {
			Integer light = Config.OVERLAY_BRIGHTNESS.getValue();
			LightmapHelper.setLightmapCoord(light, light);
		}

		cache.forEach((pos, data) -> {
			double x = pos.x + 0.5 - camera.getX(partialTicks);
			double y = pos.y + 0.05 - camera.getY(partialTicks);
			double z = pos.z + 0.5 - camera.getZ(partialTicks);

			if (Config.SHOW_ABOVE_HITBOX.getValue())
				y += data.offset;

			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glScalef(1f/32f, -1f/32f, 1f/32f);
			GL11.glRotated(90, 1, 0, 0);
			GL11.glRotated(camera.getYRot()-180, 0, 0, 1);

			float offset = Config.SHOW_SKYLIGHT_LEVEL.getValue() ? 4.5f : 0f;

			renderNumber(data.blockLightLevel, data.color, -offset);
			if (Config.SHOW_SKYLIGHT_LEVEL.getValue())
				renderNumber(data.skyLightLevel, data.color, offset);

			GL11.glPopMatrix();
		});

		if (Config.OVERLAY_TRANSPARENCY.getValue() < 100)
			GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private static void renderNumber(int light, int color, float y_offset) {
		renderNumber(light, color, y_offset, 0);
	}

	private static void renderNumber(int light, int color, float y_offset, float x_offset) {
		if (light > 15) light = 15;

		if (light >= 10) {
			x_offset -= 4;

			int secondDigit = light % 10;
			light = light / 10;

			renderNumber(secondDigit, color, y_offset, x_offset + 8);
		}

		int x = (light % 4) * 8;
		int y = (light / 4) * 8;

		Minecraft.getMinecraft().textureManager.loadTexture("/assets/lighty/textures/block/numbers.png").bind();
		drawTexture(-2.5f + x_offset, -3f + y_offset, 0, x, y, 8, 8, color);
	}

	private static void drawTexture(float x, float y, float z, float u, float v, float width, float height, int color) {
		Tessellator tessellator = Tessellator.instance;

		tessellator.startDrawingQuads();

		tessellator.setColorRGBA(
			(color >> 16) & 0xFF,
			(color >> 8) & 0xFF,
			color & 0xFF,
			(int) (2.55 * Config.OVERLAY_TRANSPARENCY.getValue())
		);

		tessellator.addVertexWithUV(x, y + height, z, u / TEXTURE_SIZE, (v + height) / TEXTURE_SIZE);
		tessellator.addVertexWithUV(x + width, y + height, z, (u + width) / TEXTURE_SIZE, (v + height) / TEXTURE_SIZE);
		tessellator.addVertexWithUV(x + width, y, z, (u + width) / TEXTURE_SIZE, v / TEXTURE_SIZE);
		tessellator.addVertexWithUV(x, y, z, u / TEXTURE_SIZE, v / TEXTURE_SIZE);

		tessellator.draw();
	}

	static class Data {
		public int blockLightLevel;
		public int skyLightLevel;
		public double offset;
		public int color;

		public Data(int blockLightLevel, int skyLightLevel, double offset, int color) {
			this.blockLightLevel = blockLightLevel;
			this.skyLightLevel = skyLightLevel;
			this.offset = offset;
			this.color = color;
		}
	}
}

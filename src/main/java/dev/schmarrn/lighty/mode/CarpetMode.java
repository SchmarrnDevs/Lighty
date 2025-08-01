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

public class CarpetMode extends LightyMode<Provider.Pos, Pair<Double, Integer>> {

	private static final float TEXTURE_SIZE = 16;

	public static void init() {
		ModeManager.registerMode(Lighty.MOD_ID+".carpet_mode", new CarpetMode());
		ModeManager.addOptions(
				() -> new OptionsCategory("gui.modeSwitcher."+Lighty.MOD_ID+".carpet_mode")
						.withComponent(Config.FLAT_CARPET.getOptionInstance())
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

	private static boolean shouldRenderSideFace(int x, int y, int z, WorldClient world, double originalHeight) {
		Block<?> block = world.getBlock(x, y, z);
		if (originalHeight > 0.0) {
			// the original block is a snow layer
			if (block != null && block.id() == Blocks.LAYER_SNOW.id()) {
				// if the other block is a snow layer,
				// we should render the face if we are strictly above the neighbor
				return originalHeight > block.getBlockBoundsFromState(world, x, y, z).maxY;
			} else {
				// if the other block is no snow layer, but air,
				// we should render the side.
				return block == null;
			}
		}
		// if it isn't a snow layer,
		// check whether there is a neighboring overlay,
		// and if not, we render the side face.
		return Provider.isBlocked(x, y, z, world);
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
			double x = pos.x -camera.getX(partialTicks);
			double y = pos.y + data.getLeft() + 0.01 -camera.getY(partialTicks);
			double z = pos.z -camera.getZ(partialTicks);

			GL11.glPushMatrix();

			Minecraft.getMinecraft().textureManager.loadTexture(Config.CARPET_TEXTURE.getValue()).bind();
			if (Config.FLAT_CARPET.getValue()) {
				GL11.glTranslated(x, y, z);
				GL11.glScalef(1f/16f, -1f/16f, 1f/16f);
				GL11.glRotated(90, 1, 0, 0);
				drawTexture(0, 0, 0, 0, 0, 16, 16, data.getRight());
			} else {
				// undo the slight upwards translation
				GL11.glTranslated(x, y - 0.01, z);
				GL11.glScalef(1f/16f, -1f/16f, 1f/16f);
				GL11.glRotated(90, 1, 0, 0);
				drawTexture(0, 0, 1, 0, 0, 16, 16, data.getRight());
				WorldClient world = minecraft.currentWorld;

				GL11.glRotated(-90, 1, 0, 0);

				// check the neighboring spawnable spots
				// if the neighbor isn't spawnable, draw the overlay
				if (shouldRenderSideFace(pos.x, pos.y, pos.z + 1, world, data.getLeft())) {
					GL11.glPushMatrix();
					drawTexture(0,-1,16,0,0, 16, 1, data.getRight());
					GL11.glPopMatrix();
				}
				if (shouldRenderSideFace(pos.x, pos.y, pos.z - 1, world, data.getLeft())) {
					GL11.glPushMatrix();
					GL11.glRotated(180, 0, 1, 0);
					drawTexture(-16,-1,0,0,0, 16, 1, data.getRight());
					GL11.glPopMatrix();
				}
				if (shouldRenderSideFace(pos.x + 1, pos.y, pos.z, world, data.getLeft())) {
					GL11.glPushMatrix();
					GL11.glRotated(90, 0, 1, 0);
					drawTexture(-16,-1,16,0,0, 16, 1, data.getRight());
					GL11.glPopMatrix();
				}
				if (shouldRenderSideFace(pos.x - 1, pos.y, pos.z, world, data.getLeft())) {
					GL11.glPushMatrix();
					GL11.glRotated(-90, 0, 1, 0);
					drawTexture(0,-1,0,0,0, 16, 1, data.getRight());
					GL11.glPopMatrix();
				}
			}

			GL11.glPopMatrix();
		});
		if (Config.OVERLAY_TRANSPARENCY.getValue() < 100)
			GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
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
}

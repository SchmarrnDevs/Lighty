package dev.schmarrn.lighty.mode;

import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.chunk.Chunk;

import javax.annotation.Nullable;

public class Provider {
	public static boolean isBlocked(int x, int y, int z, WorldClient world) {
		Block<?> block = world.getBlock(x, y, z);

		if (y < 0 || y > world.getHeightBlocks()-1)
			return true;

		return (block != null && (block.hasTag(BlockTags.PREVENT_MOB_SPAWNS) || block.getMaterial().isSolid()))
			|| !(world.isBlockNormalCube(x, y-1, z)
			&& !world.isBlockNormalCube(x, y, z)
			&& !world.getBlockMaterial(x, y, z).isLiquid());
	}

	public static @Nullable Pair<Integer, Integer> compute(WorldClient world, int x, int y, int z) {
		Minecraft minecraft = Minecraft.getMinecraft();
		WorldClient currentWorld = minecraft.currentWorld;
		if (currentWorld == null) return null;
		Chunk chunk = world.getChunkFromBlockCoords(x, z);

		int blockLightLevel = chunk.getBrightness(LightLayer.Block, x & 0xF, y+1, z & 0xF);
		int skyLightLevel = chunk.getBrightness(LightLayer.Sky, x & 0xF, y+1, z & 0xF);

		return Pair.of(blockLightLevel, skyLightLevel);
	}

	public static @Nullable Integer getColor(Pair<Integer, Integer> light) {
		return getColor(light.getLeft(), light.getRight());
	}

	public static @Nullable Integer getColor(int blockLightLevel, int skyLightLevel) {
		int color =  Config.OVERLAY_GREEN.getValue();

		if (blockLightLevel <= Config.BLOCK_THRESHOLD.getValue()) {
			if (skyLightLevel <= Config.SKY_THRESHOLD.getValue()) {
				color =  Config.OVERLAY_RED.getValue();
			} else {
				color =  Config.OVERLAY_ORANGE.getValue();
			}
		} else if (!Config.SHOW_SAFE.getValue()) {
			return null;
		}

		return color;
	}

	static class Pos {
		public int x;
		public int y;
		public int z;

		public Pos(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}

package dev.schmarrn.lighty;

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

public class LightyLevelListener implements LevelListener {
	@Override
	public void blockChanged(int i, int j, int k) {
		PlayerLocal player = Minecraft.getMinecraft().thePlayer;
		if (player == null)
			return;

		int x = Math.abs(i-(int)player.x);
		int y = Math.abs(j-(int)player.y);
		int z = Math.abs(k-(int)player.z);

		int computeDistance = Config.OVERLAY_DISTANCE.getValue() * Chunk.CHUNK_SIZE_X;

		if (x <= computeDistance && y <= computeDistance && z <= computeDistance)
			Compute.markDirty();
	}

	// We do not need anything below this, but LevelListener REALLY wants us to take the rest home…

	@Override
	public void setBlocksDirty(int i, int j, int k, int l, int m, int n) {

	}

	@Override
	public void playSound(Entity entity, String string, SoundCategory soundCategory, double d, double e, double f, float g, float h) {

	}

	@Override
	public void addParticle(String string, double d, double e, double f, double g, double h, double i, int j, double k) {

	}

	@Override
	public void addParticle(String string, double d, double e, double f, double g, double h, double i, int j) {

	}

	@Override
	public void entityAdded(Entity entity) {

	}

	@Override
	public void entityRemoved(Entity entity) {

	}

	@Override
	public void allChanged(boolean bl, boolean bl2) {

	}

	@Override
	public void playStreamingMusic(String string, String string2, int i, int j, int k) {

	}

	@Override
	public void tileEntityChanged(int i, int j, int k, TileEntity tileEntity) {

	}

	@Override
	public void levelEvent(@Nullable Player player, int i, int j, int k, int l, int m) {

	}
}

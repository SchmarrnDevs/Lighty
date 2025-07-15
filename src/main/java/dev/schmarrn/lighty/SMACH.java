package dev.schmarrn.lighty;

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.event.Compute;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;

import java.util.List;

public class SMACH {
	private static State state = State.OFF;

	public static void updateCompute(PlayerLocal player) {
		if (!Config.SHOULD_AUTO_ON.getValue()) {
			if (state == State.AUTO || state == State.OVERRIDE) {
				// if auto_on is disabled, but we somehow got stuck inside of one of the auto_on states, reset to OFF
				state = State.OFF;
				whenSwitchingToOff();
			}
			return;
		}
		// check whether we are holding an item defined as a valid auto_on item
		boolean holdsItem = false;
		List<String> activationItems = Config.AUTO_ON_ITEM_LIST.getValue();

		ItemStack heldItem = player.getHeldItem();
		if (heldItem != null) {
			Item item = Item.itemsList[heldItem.itemID];
			if (item != null) {
				String name = item.namespaceID.toString();
				holdsItem = activationItems.contains(name);
			}
		}

		// update state:
		if (state == State.OFF) {
			if (holdsItem) {
				state = State.AUTO;
			}
		} else if (state == State.AUTO || state == State.OVERRIDE) {
			// in both cases AUTO and OVERRIDE, if we let go of the item, we reset to state OFF
			if (!holdsItem) {
				state = State.OFF;
				whenSwitchingToOff();
			}
		}
		// we stay in on, because auto_on should never override on state
	}

	public static void toggle() {
		if (state == State.OFF) {
			state = State.ON;
			Compute.markDirty();
		} else if (state == State.ON) {
			state = State.OFF;
			whenSwitchingToOff();
		} else if (state == State.AUTO) {
			state = State.OVERRIDE;
			Compute.markDirty();
		} else if (state == State.OVERRIDE) {
			state = State.AUTO;
			Compute.markDirty();
		}
	}

	public static void enable() {
		if (state == State.OFF || state == State.OVERRIDE) {
			state = State.ON;
			Compute.markDirty();
		}
	}

	public static State getState() {
		return state;
	}

	public static boolean isEnabled() {
		return state == State.AUTO || state == State.ON;
	}

	private static void whenSwitchingToOff() {
		// TODO: find a more elegant solution
		ModeLoader.clear();
	}

	public enum State {
		OFF,
		ON,
		AUTO,
		OVERRIDE
	}
}

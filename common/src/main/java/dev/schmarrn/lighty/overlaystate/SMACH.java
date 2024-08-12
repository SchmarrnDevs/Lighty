package dev.schmarrn.lighty.overlaystate;

import dev.schmarrn.lighty.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class SMACH {
    private static State state = State.OFF;

    public static void updateCompute(Minecraft client) {
        if (!Config.SHOULD_AUTO_ON.getValue()) {
            if (state == State.AUTO || state == State.OVERRIDE) {
                // if auto_on is disabled, but we somehow got stuck inside of one of the auto_on states, reset to OFF
                state = State.OFF;
            }
            return;
        }
        // check whether we are holding an item defined as a valid auto_on item
        boolean holdsItem = false;
        var activationItems = Config.AUTO_ON_ITEM_LIST.getValue();

        for (var rl : activationItems) {
            Item activationItem = BuiltInRegistries.ITEM.get(rl);
            Item mainHandItem = client.player.getMainHandItem().getItem();
            Item offHandItem = client.player.getOffhandItem().getItem();

            // if we hold the activation item in our hands, set auto enabled to true.
            // if we don't, set it to false and if we aren't enabled, return early.
            if (mainHandItem == activationItem || offHandItem == activationItem) {
                holdsItem = true;
                break;
            }
        }

        // update state:
        switch (state) {
            case OFF -> {
                if (holdsItem) {
                    state = State.AUTO;
                }
            }
            case ON -> {
                // we stay in on, because auto_on should never override on state
            }
            case AUTO, OVERRIDE -> {
                // in both cases AUTO and OVERRIDE, if we let go of the item, we reset to state OFF
                if (!holdsItem) {
                    state = State.OFF;
                }
            }
        }

    }

    public static void toggle() {
        switch (state) {
            case OFF -> state = State.ON;
            case ON -> state = State.OFF;
            case AUTO -> state = State.OVERRIDE;
            case OVERRIDE -> state = State.AUTO;
        }
    }

    public static State getState() {
        return state;
    }
}

package dev.schmarrn.lighty.overlaystate;

import dev.schmarrn.lighty.config.Config;
import dev.schmarrn.lighty.core.LightyExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class SMACH {
    private static State state = State.OFF;
    private static boolean oldEnabled = false;

    public static void updateCompute() {
        Minecraft client = Minecraft.getInstance();
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
        var activationItems = Config.AUTO_ON_ITEM_LIST.getValue();

        if (client.player == null) {
            return;
        }

        Item mainHandItem = client.player.getMainHandItem().getItem();
        Item offHandItem = client.player.getOffhandItem().getItem();

        for (var rl : activationItems) {
            Item activationItem = BuiltInRegistries.ITEM.get(rl).get().value();
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
                    whenSwitchingToOff();
                }
            }
        }
        checkEnabledTransition();
    }

    public static void toggle() {
        switch (state) {
            case OFF -> state = State.ON;
            case ON -> {
                state = State.OFF;
                whenSwitchingToOff();
            }
            case AUTO -> state = State.OVERRIDE;
            case OVERRIDE -> state = State.AUTO;
        }
        checkEnabledTransition();
    }

    public static State getState() {
        return state;
    }

    public static boolean isEnabled() {
        return state == State.AUTO || state == State.ON;
    }

    private static void checkEnabledTransition() {
        var enabled = isEnabled();
        if (enabled != oldEnabled) {
            displayClientMessage();
        }
        oldEnabled = enabled;
    }

    private static void displayClientMessage() {
        Minecraft.getInstance().player.sendOverlayMessage(
                Component.translatable(
                        "lighty.overlay",
                        CommonComponents.optionStatus(isEnabled()).getString())
        );
    }

    private static void whenSwitchingToOff() {
        // TODO: find a more elegant solution
        LightyExtractor.clear();
    }
}

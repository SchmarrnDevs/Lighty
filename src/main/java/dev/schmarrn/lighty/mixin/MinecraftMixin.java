package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.LightyLevelListener;
import dev.schmarrn.lighty.event.Compute;
import dev.schmarrn.lighty.event.KeyBind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
	@Shadow
	public WorldClient currentWorld;

	@Shadow
	public PlayerLocal thePlayer;

	@Inject(method = "checkBoundInputs", at = @At("TAIL"))
	private void lighty$keybindHandler(InputDevice currentInputDevice, CallbackInfoReturnable<Boolean> cir) {
		KeyBind.callback(currentInputDevice);
	}

	@Inject(method = "runTick", at = @At("TAIL"))
	private void lighty$tick(CallbackInfo ci) {
		Compute.callback();
	}

	@Inject(method = "changeWorld(Lnet/minecraft/client/world/WorldClient;Ljava/lang/String;Lnet/minecraft/core/entity/player/Player;)V", at = @At("TAIL"))
	private void lighty$registerOnLightUpdateHandler(WorldClient world, String loadingTitle, Player player, CallbackInfo ci) {
		if (currentWorld != null)
			currentWorld.listeners.add(new LightyLevelListener());
	}
}

package dev.schmarrn.lighty.mixin;

import dev.schmarrn.lighty.ModeLoader;
import dev.schmarrn.lighty.event.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class, remap = false)
public abstract class WorldRendererMixin {
	@Shadow
	public Minecraft mc;

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Lighting;enableLight()V"))
	private void lighty$overlayRenderer(float partialTicks, long updateRenderersUntil, CallbackInfo ci) {
		if (mc.currentWorld != null && mc.thePlayer != null && ModeLoader.getCurrentMode() != null)
			Render.renderOverlay(partialTicks);
	}
}

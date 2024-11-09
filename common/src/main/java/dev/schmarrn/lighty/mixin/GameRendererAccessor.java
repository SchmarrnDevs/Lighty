package dev.schmarrn.lighty.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("bobHurt")
    void callBobHurt(PoseStack poseStack, float h);
    @Invoker("bobView")
    void callBobView(PoseStack poseStack, float h);
    @Accessor
    int getConfusionAnimationTick();
}

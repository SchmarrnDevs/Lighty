package dev.schmarrn.lighty.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.schmarrn.lighty.UtilDefinition;
import dev.schmarrn.lighty.mixin.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class IrisCompat {
    private static Matrix4f shaderFix(PoseStack stack, Camera camera, GameRenderer gameRenderer, Minecraft minecraft, LocalPlayer player) {
        // mostly taken from: https://github.com/IrisShaders/Iris/blob/1.21.5/common/src/main/java/net/irisshaders/iris/mixin/MixinModelViewBobbing.java#L98-L134
        // which in turn is mostly taken from GameRenderer
        float tickDelta = camera.getPartialTickTime();

        GameRendererAccessor gr = (GameRendererAccessor) gameRenderer;

        gr.callBobHurt(stack, tickDelta);
        if (minecraft.options.bobView().get()) {
            gr.callBobView(stack, tickDelta);
        }

        Matrix4f instance = stack.last().pose();

        float f = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float h = minecraft.options.screenEffectScale().get().floatValue();
        float i = Mth.lerp(f, player.oPortalEffectIntensity, player.portalEffectIntensity) * h * h;
        float j = player.getEffectBlendFactor(MobEffects.NAUSEA, f);
        float k = Math.max(i, j) * i * i;
        if (k > 0.0F) {
            float l = 5.0F / (k * k + 5.0F) - k * 0.04F;
            l *= l;
            Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            float m = ((float) gr.getSpinningEffectTime() + gr.getSpinningEffectSpeed()) * (float) (Math.PI / 180.0);
            instance.rotate(m, vector3f);
            instance.scale(1.0F / l, 1.0F, 1.0F);
            instance.rotate(-m, vector3f);
        }

        return instance;
    }

    public static void fixIrisShaders(PoseStack stack, Camera camera, GameRenderer gr, Minecraft minecraft) {
        if (UtilDefinition.INSTANCE.shadersEnabled() && minecraft.player != null) {
            stack.last().pose().set(shaderFix(stack, camera, gr, minecraft, minecraft.player));
        }
    }
}

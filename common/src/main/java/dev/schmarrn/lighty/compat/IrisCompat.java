package dev.schmarrn.lighty.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.schmarrn.lighty.mixin.GameRendererAccessor;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class IrisCompat {
    private static final IrisApi INSTANCE;

    static {
        IrisApi i;
        try {
            i = (IrisApi)Class.forName("net.irisshaders.iris.apiimpl.IrisApiV0Impl").getField("INSTANCE").get((Object)null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException var1) {
            i = null;
        }
        INSTANCE = i;
    }

    private static Matrix4f shaderFix(PoseStack stack, Camera camera, GameRenderer gameRenderer, Minecraft minecraft, LocalPlayer player) {
        // mostly taken from: https://github.com/IrisShaders/Iris/blob/multiloader-new/common/src/main/java/net/irisshaders/iris/mixin/MixinModelViewBobbing.java#L98-L134
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
        float i = Mth.lerp(f, player.oSpinningEffectIntensity, player.spinningEffectIntensity) * h * h;
        if (i > 0.0F) {
            int j = player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float k = 5.0F / (i * i + 5.0F) - i * 0.04F;
            k *= k;
            Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            float l = ((float) gr.getConfusionAnimationTick() + f) * (float) j * (float) (Math.PI / 180.0);
            instance.rotate(l, vector3f);
            instance.scale(1.0F / k, 1.0F, 1.0F);
            instance.rotate(-l, vector3f);
        }

        return instance;
    }

    public static void fixIrisShaders(PoseStack stack, Camera camera, GameRenderer gr, Minecraft minecraft) {
        if (IrisCompat.INSTANCE != null) {
            if (IrisCompat.INSTANCE.isShaderPackInUse()) {
                if (minecraft.player != null) {
                    stack.last().pose().set(shaderFix(stack, camera, gr, minecraft, minecraft.player));
                }
            }
        }
    }
}

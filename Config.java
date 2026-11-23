package dev.xclient.mixin.optional;

import dev.xclient.modules.SkyChanger;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The fog colour is also the clear colour, i.e. the bright band at the horizon. */
@Mixin(targets = "net.minecraft.client.renderer.fog.FogRenderer")
public abstract class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void xclient$fog(CallbackInfoReturnable<Object> cir) {
        float[] h = SkyChanger.horizon();
        if (h == null) return;
        Object o = cir.getReturnValue();
        if (o instanceof Vector4f v) cir.setReturnValue(new Vector4f(h[0], h[1], h[2], v.w));
        else if (o instanceof Vector3f) cir.setReturnValue(new Vector3f(h[0], h[1], h[2]));
    }
}

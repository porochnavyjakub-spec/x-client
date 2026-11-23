package dev.xclient.mixin.optional;

import dev.xclient.modules.SkyChanger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Optional (separate, non-required mixin config): a failure here never stops the game from starting. */
@Mixin(targets = "net.minecraft.client.renderer.SkyRenderer")
public abstract class SkyRendererMixin {
    @ModifyVariable(method = "renderSkyDisc", at = @At("HEAD"), argsOnly = true, require = 0)
    private int xclient$skyColor(int color) {
        int c = SkyChanger.skyArgb();
        return c == 0 ? color : c;
    }
}

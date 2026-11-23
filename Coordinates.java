package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", "Client-side night vision", Category.RENDER); }

    @Override
    public void onTick() {
        if (mc.player != null)
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, 0, false, false, false));
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}

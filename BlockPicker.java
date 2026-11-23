package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TriggerBot extends Module {
    public final Setting.Bool playersOnly = add(new Setting.Bool("Players only", true));
    public final Setting.Num cooldown = add(new Setting.Num("Min cooldown", 0.9, 0.5, 1.0, 0.05));

    public TriggerBot() { super("TriggerBot", "Attacks whatever is under the crosshair", Category.PVP); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || mc.gui.screen() != null) return;
        HitResult hr = mc.hitResult;
        if (hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le && le.isAlive()
                && (!playersOnly.value || le instanceof Player)) {
            if (mc.player.getAttackStrengthScale(0.5f) >= cooldown.value) {
                mc.gameMode.attack(mc.player, le);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}

package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class KillAura extends Module {
    public final Setting.Num range = add(new Setting.Num("Range", 3.6, 2.0, 6.0, 0.1));
    public final Setting.Num cooldown = add(new Setting.Num("Min cooldown", 0.92, 0.5, 1.0, 0.02));
    public final Setting.Bool playersOnly = add(new Setting.Bool("Players only", true));
    public final Setting.Bool walls = add(new Setting.Bool("Through walls", false));
    public final Setting.Bool rotate = add(new Setting.Bool("Look at target", true));

    public KillAura() { super("KillAura", "Attacks the closest target in range", Category.PVP); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (mc.gameMode == null || mc.gui.screen() != null) return;
        LivingEntity best = null;
        double bd = range.value;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || e == p || !le.isAlive() || e instanceof ArmorStand) continue;
            if (playersOnly.value && !(e instanceof Player)) continue;
            double d = p.distanceTo(e);
            if (d > bd) continue;
            if (!walls.value && !p.hasLineOfSight(e)) continue;
            best = le; bd = d;
        }
        if (best == null) return;
        if (rotate.value) {
            Vec3 d = best.getBoundingBox().getCenter().subtract(p.getEyePosition());
            p.setYRot((float) Math.toDegrees(Math.atan2(-d.x, d.z)));
            p.setXRot(Mth.clamp((float) -Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z))), -90f, 90f));
        }
        if (p.getAttackStrengthScale(0.5f) >= cooldown.value) {
            mc.gameMode.attack(p, best);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}

package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.Projection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PlayerEsp extends Module {
    public final Setting.Bool names = add(new Setting.Bool("Names", true));
    public final Setting.Bool health = add(new Setting.Bool("Health bar", true));

    public PlayerEsp() { super("PlayerESP", "2D boxes around other players (through walls)", Category.RENDER); }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive()) continue;
            Vec3 lp = p.getPosition(td);
            AABB b = p.getBoundingBox().move(lp.subtract(p.position()));
            float minX = 1e9f, minY = 1e9f, maxX = -1e9f, maxY = -1e9f;
            boolean ok = true;
            for (int i = 0; i < 8; i++) {
                float[] s = Projection.toScreen(new Vec3(
                        (i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ));
                if (s == null) { ok = false; break; }
                minX = Math.min(minX, s[0]); maxX = Math.max(maxX, s[0]);
                minY = Math.min(minY, s[1]); maxY = Math.max(maxY, s[1]);
            }
            if (!ok) continue;
            int x1 = (int) minX, y1 = (int) minY, x2 = (int) maxX, y2 = (int) maxY;
            g.outline(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2, 0xFF000000);
            g.outline(x1, y1, x2 - x1, y2 - y1, 0xFF8B5CF6);
            if (health.value) {
                float f = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
                int h = y2 - y1;
                g.fill(x1 - 5, y1, x1 - 3, y2, 0xFF222222);
                g.fill(x1 - 5, y2 - (int) (h * f), x1 - 3, y2, f > 0.5f ? 0xFF55FF55 : f > 0.25f ? 0xFFFFD155 : 0xFFFF5555);
            }
            if (names.value) {
                String s = p.getName().getString() + " " + (int) mc.player.distanceTo(p) + "m";
                g.text(mc.font, s, (x1 + x2) / 2 - mc.font.width(s) / 2, y1 - 11, 0xFFFFFFFF, true);
            }
        }
    }
}

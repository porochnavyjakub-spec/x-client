package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/** Warns when another player enters your range (base defense / AFK safety) and lists nearby players. */
public class PlayerAlert extends Module {
    public final Setting.Num range = add(new Setting.Num("Range (blocks)", 96, 16, 512, 16));
    public final Setting.Bool chat = add(new Setting.Bool("Chat message", true));
    public final Setting.Bool hud = add(new Setting.Bool("Screen alert", true));
    public final Setting.Bool list = add(new Setting.Bool("Player list", true));

    private record Alert(String text, long at) {}

    private final Set<UUID> inRange = new HashSet<>();
    private final List<Alert> alerts = new ArrayList<>();
    private final List<String> nearby = new ArrayList<>();

    public PlayerAlert() { super("PlayerAlert", "Alert when a player comes near", Category.PVP); }

    @Override public void onDisable() { inRange.clear(); alerts.clear(); nearby.clear(); }

    @Override
    public void onTick() {
        Set<UUID> now = new HashSet<>();
        List<Player> close = new ArrayList<>();
        for (Player p : mc.level.players()) {
            if (p == mc.player || mc.player.distanceTo(p) > range.value) continue;
            now.add(p.getUUID());
            close.add(p);
            if (!inRange.contains(p.getUUID())) {
                String t = p.getName().getString() + " entered range (" + (int) mc.player.distanceTo(p) + "m)";
                alerts.add(new Alert(t, System.currentTimeMillis()));
                if (chat.value) mc.player.sendSystemMessage(Component.literal("§8[§cAlert§8] §f" + t));
            }
        }
        inRange.clear();
        inRange.addAll(now);
        alerts.removeIf(a -> System.currentTimeMillis() - a.at > 6000);
        if (alerts.size() > 4) alerts.subList(0, alerts.size() - 4).clear();

        close.sort(Comparator.comparingDouble(mc.player::distanceTo));
        nearby.clear();
        for (Player p : close) {
            nearby.add(p.getName().getString() + "  " + (int) mc.player.distanceTo(p) + "m  "
                    + (int) Math.ceil(p.getHealth()) + "hp");
            if (nearby.size() >= 8) break;
        }
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        if (hud.value) {
            int y = 40;
            for (Alert a : alerts) {
                g.text(mc.font, a.text, sw / 2 - mc.font.width(a.text) / 2, y, 0xFFFF5555, true);
                y += 12;
            }
        }
        if (list.value) {
            int y = sh / 2 - 20;
            for (String s : nearby) {
                g.fill(4, y - 1, 6, y + 9, 0xFF8B5CF6);
                g.text(mc.font, s, 10, y, 0xFFE8E8EC, true);
                y += 12;
            }
        }
    }
}

package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.Draw;
import dev.xclient.util.Projection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listens to sounds the server sends and reports redstone devices, players and villagers
 * (label, distance and direction) in the HUD, as world markers and optionally in chat.
 * Range is decided by the server (about 16 blocks, louder sounds travel further).
 */
public class SoundDetector extends Module {
    public static SoundDetector INSTANCE;

    private static final int REDSTONE = 0, PLAYER = 1, VILLAGER = 2;
    private static final int[] COLORS = {0xFFFF5555, 0xFF55D6FF, 0xFF6BFF8A};
    private static final String[] REDSTONE_KEYS = {"piston", "dispenser", "note_block", "door", "fence_gate", "button",
            "lever", "pressure_plate", "tripwire", "comparator", "redstone", "tnt", "hopper", "bell"};
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    public final Setting.Bool redstone = add(new Setting.Bool("Redstone", true));
    public final Setting.Bool players = add(new Setting.Bool("Players", true));
    public final Setting.Bool villagers = add(new Setting.Bool("Villagers", true));
    public final Setting.Bool chat = add(new Setting.Bool("Chat messages", false));
    public final Setting.Bool markers = add(new Setting.Bool("World markers", true));
    public final Setting.Num time = add(new Setting.Num("Show time (s)", 8, 2, 30, 1));

    private static final class Ping {
        final String label; final int cat; final Vec3 pos; long at;
        Ping(String l, int c, Vec3 p, long a) { label = l; cat = c; pos = p; at = a; }
    }

    private final List<Ping> pings = new ArrayList<>();
    private final Map<String, Long> lastChat = new HashMap<>();

    public SoundDetector() {
        super("SoundESP", "Redstone, player and villager sounds", Category.RENDER);
        INSTANCE = this;
    }

    private static int classify(String id) {
        if (id.startsWith("entity.villager.") || id.startsWith("entity.wandering_trader.")) return VILLAGER;
        if (id.startsWith("entity.player.") || id.startsWith("item.armor.equip") || id.startsWith("item.elytra")
                || id.startsWith("item.totem") || id.startsWith("item.shield")
                || (id.startsWith("block.") && (id.endsWith(".step") || id.endsWith(".place") || id.endsWith(".break")))
                || id.startsWith("block.chest.") || id.startsWith("block.barrel.") || id.startsWith("block.shulker_box.")
                || id.startsWith("block.ender_chest.")) return PLAYER;
        for (String k : REDSTONE_KEYS) if (id.contains(k)) return REDSTONE;
        return -1;
    }

    private static String pretty(String id) {
        String s = id.contains(".") ? id.substring(id.indexOf('.') + 1) : id;
        s = s.replace('_', ' ').replace(".", " · ");
        return s.isEmpty() ? id : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Called from the sound packet mixin (main thread). */
    public void onSound(String rawId, double x, double y, double z) {
        if (!enabled || mc.player == null) return;
        String id = rawId.startsWith("minecraft:") ? rawId.substring(10) : rawId;
        int cat = classify(id);
        if (cat < 0) return;
        if ((cat == REDSTONE && !redstone.value) || (cat == PLAYER && !players.value) || (cat == VILLAGER && !villagers.value)) return;

        Vec3 pos = new Vec3(x, y, z);
        String label = pretty(id);
        long now = System.currentTimeMillis();
        for (Ping p : pings) {
            if (p.label.equals(label) && p.pos.distanceToSqr(pos) < 4 && now - p.at < 1000) { p.at = now; return; }
        }
        pings.add(new Ping(label, cat, pos, now));
        if (pings.size() > 40) pings.remove(0);

        if (chat.value && now - lastChat.getOrDefault(label, 0L) > 1500) {
            lastChat.put(label, now);
            Vec3 d = pos.subtract(mc.player.position());
            mc.player.sendSystemMessage(Component.literal(
                    "§8[§bSound§8] §f" + label + " §7" + (int) d.length() + "m " + arrow(d)));
        }
    }

    private String arrow(Vec3 d) {
        double yawTo = Math.toDegrees(Math.atan2(-d.x, d.z));
        double rel = Mth.wrapDegrees(yawTo - mc.player.getYRot());
        return ARROWS[(int) Math.round(((rel + 360) % 360) / 45.0) % 8];
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        long ttl = (long) (time.value * 1000);
        pings.removeIf(p -> now - p.at > ttl);
    }

    @Override
    public void onDisable() { pings.clear(); }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        int y = 26, shown = 0;
        for (int i = pings.size() - 1; i >= 0 && shown < 8; i--, shown++) {
            Ping p = pings.get(i);
            Vec3 d = p.pos.subtract(mc.player.position());
            String txt = arrow(d) + " " + p.label + "  " + (int) d.length() + "m";
            Draw.rrect(g, 4, y - 2, mc.font.width(txt) + 14, 12, 0x99000000);
            g.fill(4, y - 2, 6, y + 10, COLORS[p.cat]);
            g.text(mc.font, txt, 10, y, COLORS[p.cat], true);
            y += 14;
        }
        if (!markers.value) return;
        for (Ping p : pings) {
            float[] s = Projection.toScreen(p.pos);
            if (s == null) continue;
            int x = (int) s[0], yy = (int) s[1];
            g.fill(x - 3, yy - 3, x + 3, yy + 3, COLORS[p.cat]);
            g.outline(x - 4, yy - 4, 8, 8, 0xFF000000);
            String t = p.label + " " + (int) p.pos.distanceTo(mc.player.position()) + "m";
            g.text(mc.font, t, x - mc.font.width(t) / 2, yy - 16, COLORS[p.cat], true);
        }
    }
}

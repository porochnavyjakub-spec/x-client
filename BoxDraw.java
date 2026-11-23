package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.ChunkScanner;
import dev.xclient.util.ChunkScanner.Info;
import dev.xclient.util.Draw;
import dev.xclient.util.Projection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Finds "sus" chunks: loaded chunks with an unusual amount of player-made things
 * (hoppers, ender chests, shulkers, storage, rotated deepslate, ...). Shows a chunk minimap,
 * labels in the world and optional chat alerts. Only chunks inside your render distance can be seen.
 */
public class SusChunk extends Module {
    public final Setting.Num range = add(new Setting.Num("Range (chunks)", 10, 3, 16, 1));
    public final Setting.Num minScore = add(new Setting.Num("Min score", 12, 3, 100, 1));
    public final Setting.Bool minimap = add(new Setting.Bool("Chunk minimap", true));
    public final Setting.Bool labels = add(new Setting.Bool("World labels", true));
    public final Setting.Bool chat = add(new Setting.Bool("Chat alerts", true));

    private List<Info> sus = List.of();
    private final Set<Long> announced = new HashSet<>();
    private int ticks;

    public SusChunk() { super("SusChunk", "Highlights chunks with player activity (bases)", Category.RENDER); }

    @Override public void onEnable() { announced.clear(); sus = List.of(); }
    @Override public void onDisable() { announced.clear(); sus = List.of(); }

    @Override
    public void onTick() {
        ChunkScanner.I.want((int) Math.round(range.value));
        if (++ticks % 10 != 0) return;
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
        int r = (int) Math.round(range.value);
        List<Info> out = new ArrayList<>();
        for (Info i : ChunkScanner.I.chunks.values()) {
            if (Math.abs(i.cx - pcx) > r || Math.abs(i.cz - pcz) > r) continue;
            if (i.score >= minScore.value) out.add(i);
        }
        out.sort((a, b) -> Integer.compare(b.score, a.score));
        sus = out.size() > 30 ? new ArrayList<>(out.subList(0, 30)) : out;

        if (chat.value) {
            for (Info i : sus) {
                if (!announced.add(ChunkScanner.key(i.cx, i.cz))) continue;
                int bx = i.cx * 16 + 8, bz = i.cz * 16 + 8;
                double dist = Math.sqrt(Math.pow(bx - mc.player.getX(), 2) + Math.pow(bz - mc.player.getZ(), 2));
                mc.player.sendSystemMessage(Component.literal("§8[§cSus§8] §fchunk §7" + bx + ", " + bz
                        + " §fscore §c" + i.score + " §7(" + (int) dist + "m) §8" + ChunkScanner.summary(i)));
            }
        }
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        if (labels.value) {
            int shown = 0;
            for (Info i : sus) {
                if (shown >= 6) break;
                float[] s = Projection.toScreen(new Vec3(i.cx * 16 + 8, i.avgY, i.cz * 16 + 8));
                if (s == null) continue;
                shown++;
                double dx = i.cx * 16 + 8 - mc.player.getX(), dz = i.cz * 16 + 8 - mc.player.getZ();
                String t = "SUS " + i.score + "  " + (int) Math.sqrt(dx * dx + dz * dz) + "m";
                int x = (int) s[0], y = (int) s[1];
                g.fill(x - 3, y - 3, x + 3, y + 3, 0xFFFF5555);
                g.outline(x - 4, y - 4, 8, 8, 0xFF000000);
                g.text(mc.font, t, x - mc.font.width(t) / 2, y - 16, 0xFFFF7777, true);
            }
        }
        if (minimap.value) drawMap(g);
    }

    private void drawMap(GuiGraphicsExtractor g) {
        int R = Math.min(8, (int) Math.round(range.value)), cell = 5, n = 2 * R + 1;
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int x0 = sw - n * cell - 8, y0 = sh - n * cell - 44;
        Draw.rrect(g, x0 - 4, y0 - 14, n * cell + 8, n * cell + 18, 0xCC0E0F11);
        g.text(mc.font, "Sus chunks: " + sus.size(), x0, y0 - 11, 0xFFFF7777, false);
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
        for (int dz = -R; dz <= R; dz++)
            for (int dx = -R; dx <= R; dx++) {
                Info i = ChunkScanner.I.chunks.get(ChunkScanner.key(pcx + dx, pcz + dz));
                int col;
                if (i == null) col = 0xFF1A1B1F;
                else if (i.score >= minScore.value) col = 0xFF000000 | (Math.min(255, 130 + i.score * 3) << 16) | 0x2020;
                else if (i.score > 0) col = 0xFF5A5230;
                else col = 0xFF2E3036;
                int cx = x0 + (dx + R) * cell, cy = y0 + (dz + R) * cell;
                g.fill(cx, cy, cx + cell - 1, cy + cell - 1, col);
            }
        g.outline(x0 + R * cell - 1, y0 + R * cell - 1, cell + 1, cell + 1, 0xFFFFFFFF); // you (north is up)
    }
}

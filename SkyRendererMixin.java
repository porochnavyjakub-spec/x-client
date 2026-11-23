package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.BoxDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;

/** Highlights any blocks you pick (search + multi-select) in the chunks the server has sent you. */
public class BlockEsp extends Module {
    public final Setting.Blocks blocks = add(new Setting.Blocks("Blocks", Blocks.ANCIENT_DEBRIS, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE));
    public final Setting.Num range = add(new Setting.Num("Range (chunks)", 5, 2, 10, 1));
    public final Setting.Num maxBoxes = add(new Setting.Num("Max boxes", 250, 50, 600, 50));

    private record Hit(int x, int y, int z, int color) {}
    private final Map<Long, List<Hit>> cache = new HashMap<>();
    private final Map<Long, Long> scanned = new HashMap<>();
    private List<Hit> visible = List.of();
    private int sig, ticks;

    public BlockEsp() { super("BlockESP", "Boxes around the blocks you choose", Category.RENDER); }

    @Override public void onEnable() { cache.clear(); scanned.clear(); visible = List.of(); }
    @Override public void onDisable() { cache.clear(); scanned.clear(); visible = List.of(); }

    private static int colorOf(Block b) {
        int h = BuiltInRegistries.BLOCK.getKey(b).hashCode() * 0x9E3779B1;
        return 0xFF000000 | (0x70 + ((h >> 16) & 0x8F)) << 16 | (0x70 + ((h >> 8) & 0x8F)) << 8 | (0x70 + (h & 0x8F));
    }

    @Override
    public void onTick() {
        var w = mc.level;
        int s = blocks.value.hashCode();
        if (s != sig) { sig = s; cache.clear(); scanned.clear(); }
        if (blocks.value.isEmpty()) { visible = List.of(); return; }
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4, r = (int) Math.round(range.value);
        long now = w.getGameTime();
        int minY = w.dimension().equals(Level.OVERWORLD) ? -64 : 0;

        List<int[]> todo = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) {
                long k = ((long) (pcx + dx) << 32) | ((pcz + dz) & 0xFFFFFFFFL);
                Long at = scanned.get(k);
                if (at == null || now - at > 400) todo.add(new int[]{pcx + dx, pcz + dz, at == null ? 0 : 1, dx * dx + dz * dz});
            }
        todo.sort(Comparator.<int[]>comparingInt(a -> a[2]).thenComparingInt(a -> a[3]));
        long end = System.nanoTime() + 3_000_000L;
        for (int[] t : todo) {
            if (System.nanoTime() > end) break;
            LevelChunk c = w.getChunkSource().getChunkNow(t[0], t[1]);
            if (c == null) continue;
            long k = ((long) t[0] << 32) | (t[1] & 0xFFFFFFFFL);
            cache.put(k, scan(c, t[0], t[1], minY));
            scanned.put(k, now);
        }
        cache.keySet().removeIf(k -> Math.abs((int) (k >> 32) - pcx) > r + 2 || Math.abs((int) (long) k - pcz) > r + 2);

        if (++ticks % 10 == 0) {
            double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
            List<Hit> all = new ArrayList<>();
            for (List<Hit> l : cache.values()) all.addAll(l);
            all.sort(Comparator.comparingDouble((Hit h) -> Math.pow(h.x - px, 2) + Math.pow(h.y - py, 2) + Math.pow(h.z - pz, 2)));
            int cap = (int) Math.round(maxBoxes.value);
            visible = all.size() > cap ? new ArrayList<>(all.subList(0, cap)) : all;
        }
    }

    private List<Hit> scan(LevelChunk c, int cx, int cz, int minY) {
        List<Hit> out = new ArrayList<>();
        Set<Block> set = blocks.value;
        LevelChunkSection[] secs = c.getSections();
        for (int si = 0; si < secs.length; si++) {
            LevelChunkSection s = secs[si];
            if (s == null || s.hasOnlyAir() || !s.maybeHas((BlockState st) -> set.contains(st.getBlock()))) continue;
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int x = 0; x < 16; x++) {
                        Block b = s.getBlockState(x, y, z).getBlock();
                        if (set.contains(b) && out.size() < 2000)
                            out.add(new Hit((cx << 4) + x, minY + si * 16 + y, (cz << 4) + z, colorOf(b)));
                    }
        }
        return out;
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        for (Hit h : visible) BoxDraw.block(g, h.x, h.y, h.z, h.color);
    }
}

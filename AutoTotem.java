package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.BoxDraw;
import dev.xclient.util.ChunkScanner;
import dev.xclient.util.ChunkScanner.Found;
import dev.xclient.util.ChunkScanner.Info;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Natural deepslate always has axis=Y, so rotated deepslate means a player placed it (tunnels, bases). */
public class DeepslateFinder extends Module {
    public final Setting.Num range = add(new Setting.Num("Range (blocks)", 80, 16, 200, 8));
    public final Setting.Num minPerChunk = add(new Setting.Num("Min per chunk", 2, 1, 20, 1));

    private List<Found> list = List.of();
    private int ticks;

    public DeepslateFinder() { super("DeepslateFinder", "Finds player-placed (rotated) deepslate", Category.RENDER); }

    @Override
    public void onTick() {
        ChunkScanner.I.want((int) Math.ceil(range.value / 16.0) + 1);
        if (++ticks % 10 != 0) return;
        double max = range.value, px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        List<Found> out = new ArrayList<>();
        for (Info i : ChunkScanner.I.chunks.values()) {
            if (i.counts[ChunkScanner.K_ROTATED] < minPerChunk.value) continue;
            for (Found f : i.found) {
                if (f.kind() != ChunkScanner.K_ROTATED) continue;
                double dx = f.x() + .5 - px, dy = f.y() + .5 - py, dz = f.z() + .5 - pz;
                if (dx * dx + dy * dy + dz * dz <= max * max) out.add(f);
            }
        }
        out.sort(Comparator.comparingDouble((Found f) -> {
            double dx = f.x() + .5 - px, dy = f.y() + .5 - py, dz = f.z() + .5 - pz;
            return dx * dx + dy * dy + dz * dz;
        }));
        list = out.size() > 300 ? new ArrayList<>(out.subList(0, 300)) : out;
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        for (Found f : list) BoxDraw.block(g, f.x(), f.y(), f.z(), ChunkScanner.COLORS[ChunkScanner.K_ROTATED]);
    }

    @Override public void onDisable() { list = List.of(); }
}

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

public class StorageEsp extends Module {
    public final Setting.Num range = add(new Setting.Num("Range (blocks)", 64, 16, 160, 8));
    public final Setting.Bool chests = add(new Setting.Bool("Chests / barrels", true));
    public final Setting.Bool ender = add(new Setting.Bool("Ender chests", true));
    public final Setting.Bool shulkers = add(new Setting.Bool("Shulker boxes", true));
    public final Setting.Bool furnaces = add(new Setting.Bool("Furnaces", false));
    public final Setting.Bool hoppers = add(new Setting.Bool("Hoppers / dispensers", true));
    public final Setting.Bool spawners = add(new Setting.Bool("Spawners", true));

    private List<Found> list = List.of();
    private int ticks;

    public StorageEsp() { super("StorageESP", "Boxes around storage blocks through walls", Category.RENDER); }

    private boolean wants(int k) {
        return switch (k) {
            case ChunkScanner.K_CHEST, ChunkScanner.K_BARREL -> chests.value;
            case ChunkScanner.K_ENDER -> ender.value;
            case ChunkScanner.K_SHULKER -> shulkers.value;
            case ChunkScanner.K_FURNACE -> furnaces.value;
            case ChunkScanner.K_HOPPER, ChunkScanner.K_DISPENSER -> hoppers.value;
            case ChunkScanner.K_SPAWNER -> spawners.value;
            default -> false;
        };
    }

    @Override
    public void onTick() {
        ChunkScanner.I.want((int) Math.ceil(range.value / 16.0) + 1);
        if (++ticks % 10 != 0) return;
        double max = range.value, px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        List<Found> out = new ArrayList<>();
        for (Info i : ChunkScanner.I.chunks.values())
            for (Found f : i.found) {
                if (!wants(f.kind())) continue;
                double dx = f.x() + .5 - px, dy = f.y() + .5 - py, dz = f.z() + .5 - pz;
                if (dx * dx + dy * dy + dz * dz <= max * max) out.add(f);
            }
        out.sort(Comparator.comparingDouble((Found f) -> {
            double dx = f.x() + .5 - px, dy = f.y() + .5 - py, dz = f.z() + .5 - pz;
            return dx * dx + dy * dy + dz * dz;
        }));
        list = out.size() > 250 ? new ArrayList<>(out.subList(0, 250)) : out;
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        for (Found f : list) BoxDraw.block(g, f.x(), f.y(), f.z(), ChunkScanner.COLORS[f.kind()]);
    }

    @Override public void onDisable() { list = List.of(); }
}

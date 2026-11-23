package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.BoxDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** PvP helper: 1x1 holes surrounded by bedrock (green) or obsidian-type blocks (yellow). */
public class HoleEsp extends Module {
    private static final Set<Block> SAFE = Set.of(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN,
            Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.ENDER_CHEST);

    public final Setting.Num range = add(new Setting.Num("Range", 8, 3, 12, 1));

    private record Hole(int x, int y, int z, boolean bedrock) {}

    private List<Hole> holes = List.of();
    private int ticks;
    private final BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

    public HoleEsp() { super("HoleESP", "Highlights safe holes for crystal PvP", Category.RENDER); }

    private Block block(int x, int y, int z) { return mc.level.getBlockState(mp.set(x, y, z)).getBlock(); }
    private boolean air(int x, int y, int z) { return mc.level.getBlockState(mp.set(x, y, z)).isAir(); }

    @Override
    public void onTick() {
        if (++ticks % 5 != 0) return;
        int r = (int) Math.round(range.value);
        int px = mc.player.getBlockX(), py = mc.player.getBlockY(), pz = mc.player.getBlockZ();
        List<Hole> out = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++)
                for (int dy = -4; dy <= 3; dy++) {
                    int x = px + dx, y = py + dy, z = pz + dz;
                    if (!air(x, y, z) || !air(x, y + 1, z)) continue;
                    Block floor = block(x, y - 1, z);
                    if (!SAFE.contains(floor)) continue;
                    Block a = block(x + 1, y, z), b = block(x - 1, y, z), c = block(x, y, z + 1), d = block(x, y, z - 1);
                    if (!SAFE.contains(a) || !SAFE.contains(b) || !SAFE.contains(c) || !SAFE.contains(d)) continue;
                    boolean bed = floor == Blocks.BEDROCK && a == Blocks.BEDROCK && b == Blocks.BEDROCK
                            && c == Blocks.BEDROCK && d == Blocks.BEDROCK;
                    out.add(new Hole(x, y, z, bed));
                }
        out.sort(Comparator.comparingDouble((Hole h) -> {
            double dx = h.x + .5 - mc.player.getX(), dz = h.z + .5 - mc.player.getZ();
            return dx * dx + dz * dz;
        }));
        holes = out.size() > 80 ? new ArrayList<>(out.subList(0, 80)) : out;
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        for (Hole h : holes) BoxDraw.box(g, h.x, h.y, h.z, 1, 0.1, 1, h.bedrock ? 0xFF55FF55 : 0xFFFFD155);
    }

    @Override public void onDisable() { holes = List.of(); }
}

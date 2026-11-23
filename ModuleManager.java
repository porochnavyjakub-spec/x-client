package dev.xclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

/** 2D screen-space box around a world-space cuboid (drawn over everything, so it works through walls). */
public final class BoxDraw {
    public static void box(GuiGraphicsExtractor g, double x, double y, double z, double w, double h, double d, int color) {
        float minX = 1e9f, minY = 1e9f, maxX = -1e9f, maxY = -1e9f;
        for (int i = 0; i < 8; i++) {
            float[] s = Projection.toScreen(new Vec3(
                    x + ((i & 1) == 0 ? 0 : w), y + ((i & 2) == 0 ? 0 : h), z + ((i & 4) == 0 ? 0 : d)));
            if (s == null) return;
            minX = Math.min(minX, s[0]); maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]); maxY = Math.max(maxY, s[1]);
        }
        if (maxX < -20 || minX > 4000 || maxY < -20 || minY > 4000) return;
        int x1 = (int) minX, y1 = (int) minY;
        int x2 = Math.max(x1 + 3, (int) maxX), y2 = Math.max(y1 + 3, (int) maxY);
        g.fill(x1, y1, x2, y2, (color & 0xFFFFFF) | 0x40000000);
        g.outline(x1, y1, x2 - x1, y2 - y1, color);
    }

    public static void block(GuiGraphicsExtractor g, int x, int y, int z, int color) {
        box(g, x, y, z, 1, 1, 1, color);
    }
}

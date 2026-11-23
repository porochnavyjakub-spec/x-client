package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.Draw;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Top centre: XYZ and facing direction. */
public class Coordinates extends Module {
    public Coordinates() { super("Coordinates", "XYZ and direction, top centre", Category.HUD); enabled = true; }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        var p = mc.player;
        String s = p.getBlockX() + "  " + p.getBlockY() + "  " + p.getBlockZ() + "   " + p.getDirection().getName();
        int w = mc.font.width(s), x = (mc.getWindow().getGuiScaledWidth() - w) / 2;
        Draw.rrect(g, x - 6, 4, w + 12, 14, 0x99000000);
        g.fillGradient(x - 6, 16, x + w + 6, 18, 0xFF7C6CFF, 0xFF38D6FF);
        g.text(mc.font, s, x, 8, 0xFFEDEDF5, true);
    }
}

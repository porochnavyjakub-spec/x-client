package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/** Totem icon + count next to the hotbar (bottom left of it). */
public class TotemCounter extends Module {
    private static final Identifier TOTEM = Identifier.withDefaultNamespace("textures/item/totem_of_undying.png");

    public TotemCounter() { super("TotemCounter", "Totems you carry, next to the hotbar", Category.HUD); enabled = true; }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        var p = mc.player;
        int n = p.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? p.getOffhandItem().getCount() : 0;
        for (int i = 0; i < 36; i++) {
            var s = p.getInventory().getItem(i);
            if (s.is(Items.TOTEM_OF_UNDYING)) n += s.getCount();
        }
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int x = sw / 2 - 91 - 44, y = sh - 21;
        g.fill(x - 3, y - 3, x + 36, y + 19, 0x99000000);
        g.outline(x - 3, y - 3, 39, 22, n > 0 ? 0xFF7C6CFF : 0xFFB23B4D);
        g.blit(RenderPipelines.GUI_TEXTURED, TOTEM, x, y, 0, 0, 16, 16, 16, 16);
        g.text(mc.font, "x" + n, x + 18, y + 4, n > 0 ? 0xFFEDEDF5 : 0xFFFF6B7D, true);
    }
}

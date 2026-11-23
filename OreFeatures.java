package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** "Modules on": bottom right, lists the enabled modules. */
public class ModuleList extends Module {
    public ModuleList() { super("ModuleList", "Enabled modules (bottom right)", Category.HUD); enabled = true; }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        List<String> names = new ArrayList<>();
        for (Module m : ModuleManager.ALL) if (m.enabled && m.category != Category.HUD) names.add(m.name);
        names.sort(Comparator.comparingInt((String n) -> mc.font.width(n)).reversed());
        int sw = mc.getWindow().getGuiScaledWidth(), y = mc.getWindow().getGuiScaledHeight() - 4;
        for (int i = names.size() - 1; i >= 0; i--) {
            String n = names.get(i);
            int w = mc.font.width(n);
            y -= 12;
            g.fill(sw - w - 10, y, sw - 2, y + 11, 0x99000000);
            g.fill(sw - 4, y, sw - 2, y + 11, 0xFF38D6FF);
            g.text(mc.font, n, sw - w - 7, y + 2, 0xFFEDEDF5, true);
        }
    }
}

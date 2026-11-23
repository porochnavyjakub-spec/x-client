package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Armor bar above the hotbar: item icons with a durability bar (and percent). */
public class ArmorHud extends Module {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Map<Identifier, Boolean> exists = new HashMap<>();

    public final Setting.Bool percent = add(new Setting.Bool("Percent", true));

    public ArmorHud() { super("ArmorHud", "Armor icons + durability above the hotbar", Category.HUD); enabled = true; }

    private boolean hasTexture(Identifier id) {
        return exists.computeIfAbsent(id, k -> mc.getResourceManager().getResource(k).isPresent());
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int x = sw / 2 + 12, y = sh - 66;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack s = mc.player.getItemBySlot(slot);
            if (!s.isEmpty()) {
                Identifier tex = Identifier.withDefaultNamespace(
                        "textures/item/" + BuiltInRegistries.ITEM.getKey(s.getItem()).getPath() + ".png");
                if (hasTexture(tex)) g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, 16, 16, 16, 16);
                else g.text(mc.font, "?", x + 5, y + 4, 0xFFEDEDF5, true);
                if (s.getMaxDamage() > 0) {
                    float f = 1f - (float) s.getDamageValue() / s.getMaxDamage();
                    int col = f > 0.5f ? 0xFF55FF7A : f > 0.25f ? 0xFFFFD155 : 0xFFFF5566;
                    g.fill(x + 1, y + 17, x + 15, y + 19, 0xFF000000);
                    g.fill(x + 1, y + 17, x + 1 + (int) (14 * f), y + 18, col);
                    if (percent.value) {
                        String t = (int) (f * 100) + "%";
                        g.text(mc.font, t, x + 8 - mc.font.width(t) / 2, y + 20, col, true);
                    }
                }
            }
            x += 22;
        }
    }
}

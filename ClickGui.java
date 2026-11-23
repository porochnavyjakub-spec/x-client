package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    public final Setting.Num delay = add(new Setting.Num("Delay (ticks)", 1, 0, 10, 1));
    private int wait;

    public AutoTotem() { super("AutoTotem", "Keeps a totem in your offhand", Category.PVP); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p == null || mc.gameMode == null || mc.gui.screen() != null) return;
        if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) { wait = 0; return; }
        if (wait++ < delay.value) return;
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                int slot = i < 9 ? 36 + i : i; // inventory menu: hotbar 36-44, main 9-35
                mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId, slot, 40, ClickType.SWAP, p);
                wait = 0;
                return;
            }
        }
    }
}

package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.gui.InspectScreen;
import dev.xclient.modules.Knives.Knife;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Shows your swords as CS2 knives (model + name), client side only.
 * The skin is put on the stacks in your hotbar / offhand and removed again whenever a screen is open,
 * so the server never sees changed stacks in inventory clicks.
 */
public class KnifeSkins extends Module {
    public static KnifeSkins INSTANCE;

    public final Setting.Mode knife = add(new Setting.Mode("Knife", Knives.labels()));
    public final Setting.Bool names = add(new Setting.Bool("CS2 names", true));

    private static final class Applied {
        Identifier prevModel;
        Component ourName;      // non-null if we set the custom name
    }

    private final Map<ItemStack, Applied> applied = new IdentityHashMap<>();

    public KnifeSkins() {
        super("KnifeSkins", "Swords look like CS2 knives (I = inspect)", Category.CLIENT);
        INSTANCE = this;
    }

    public Knife current() { return Knives.ALL.get(knife.value); }

    /** Opens the CS2-style inspect screen for the selected knife (key I). */
    public static void inspect() {
        KnifeSkins k = INSTANCE;
        if (k == null || !k.enabled) return;
        net.minecraft.client.Minecraft.getInstance().gui.setScreen(new InspectScreen(k.current()));
    }

    @Override public void onDisable() { revert(); }

    @Override
    public void onTick() {
        if (mc.gui.screen() != null) { revert(); return; }
        Knife k = current();
        Identifier model = Identifier.fromNamespaceAndPath("xclient", k.id());
        Component name = Component.literal(k.name()).withStyle(s -> s.withItalic(false).withColor(k.color() & 0xFFFFFF));

        Map<ItemStack, Applied> now = new IdentityHashMap<>();
        for (int i = 0; i < 9; i++) apply(mc.player.getInventory().getItem(i), model, name, now);
        apply(mc.player.getOffhandItem(), model, name, now);

        for (Map.Entry<ItemStack, Applied> e : applied.entrySet())
            if (!now.containsKey(e.getKey())) undo(e.getKey(), e.getValue());
        applied.clear();
        applied.putAll(now);
    }

    private void apply(ItemStack s, Identifier model, Component name, Map<ItemStack, Applied> now) {
        if (s.isEmpty() || !s.is(ItemTags.SWORDS)) return;
        Applied a = applied.get(s);
        if (a == null) {
            a = new Applied();
            a.prevModel = s.get(DataComponents.ITEM_MODEL);
        }
        s.set(DataComponents.ITEM_MODEL, model);
        if (names.value && (s.get(DataComponents.CUSTOM_NAME) == null || a.ourName != null)) {
            s.set(DataComponents.CUSTOM_NAME, name);
            a.ourName = name;
        } else if (!names.value && a.ourName != null) {
            s.remove(DataComponents.CUSTOM_NAME);
            a.ourName = null;
        }
        now.put(s, a);
    }

    private static void undo(ItemStack s, Applied a) {
        if (a.prevModel != null) s.set(DataComponents.ITEM_MODEL, a.prevModel);
        else s.remove(DataComponents.ITEM_MODEL);
        if (a.ourName != null) s.remove(DataComponents.CUSTOM_NAME);
    }

    private void revert() {
        for (Map.Entry<ItemStack, Applied> e : applied.entrySet()) undo(e.getKey(), e.getValue());
        applied.clear();
    }
}

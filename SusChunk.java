package dev.xclient;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xclient.core.Config;
import dev.xclient.core.ModuleManager;
import dev.xclient.gui.ClickGui;
import dev.xclient.modules.KnifeSkins;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class XClient implements ClientModInitializer {
    public static KeyMapping guiKey;
    public static KeyMapping inspectKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("xclient", "main"));
        guiKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.xclient.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, cat));
        inspectKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.xclient.inspect", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, cat));
        ModuleManager.registerKeys(cat);
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.consumeClick()) mc.gui.setScreen(new ClickGui());
            while (inspectKey.consumeClick()) { if (mc.player != null && mc.gui.screen() == null) KnifeSkins.inspect(); }
            ModuleManager.tick(mc);
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("xclient", "overlay"), ModuleManager::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}

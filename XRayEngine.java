package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.util.Draw;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Top right: FPS, ping, speed and other info. */
public class InfoHud extends Module {
    public final Setting.Bool fps = add(new Setting.Bool("FPS", true));
    public final Setting.Bool ping = add(new Setting.Bool("Ping", true));
    public final Setting.Bool speed = add(new Setting.Bool("Speed", true));
    public final Setting.Bool clock = add(new Setting.Bool("Clock", true));

    private int frames, shownFps;
    private long lastSec = System.currentTimeMillis();
    private double px, pz, blocksPerSec;
    private boolean havePos;

    public InfoHud() { super("InfoHud", "FPS, ping, speed, clock (top right)", Category.HUD); enabled = true; }

    @Override
    public void onTick() {
        var p = mc.player;
        if (havePos) blocksPerSec = Math.hypot(p.getX() - px, p.getZ() - pz) * 20.0;
        px = p.getX(); pz = p.getZ(); havePos = true;
    }

    @Override
    public void onRender(GuiGraphicsExtractor g, float td) {
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastSec >= 1000) { shownFps = frames; frames = 0; lastSec = now; }

        List<String> lines = new ArrayList<>();
        if (fps.value) lines.add("FPS " + shownFps);
        if (ping.value && mc.getConnection() != null) {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) lines.add("Ping " + info.getLatency());
        }
        if (speed.value) lines.add("Speed " + String.format("%.1f", blocksPerSec));
        if (clock.value) lines.add(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        int sw = mc.getWindow().getGuiScaledWidth(), y = 4;
        for (String s : lines) {
            int w = mc.font.width(s);
            Draw.rrect(g, sw - w - 12, y, w + 10, 13, 0x99000000);
            g.fill(sw - 4, y + 2, sw - 2, y + 11, 0xFF7C6CFF);
            g.text(mc.font, s, sw - w - 7, y + 3, 0xFFEDEDF5, true);
            y += 15;
        }
    }
}

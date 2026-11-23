package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;

/**
 * Replaces the sky / horizon colours with presets inspired by photos: sun rays, storm, sunset,
 * night sky and clear blue. Turn the module off for the vanilla sky.
 * Works through optional mixins (see mixin/optional); if they do not apply to your exact game
 * version the game still starts, the sky just stays vanilla.
 */
public class SkyChanger extends Module {
    public static SkyChanger INSTANCE;

    // zenith, horizon (RGB)
    private static final int[][] PRESETS = {
            {0x3F6FA8, 0xC9D6DD},   // sun rays through clouds
            {0x4B4E55, 0x80838A},   // storm clouds
            {0x8B5CB8, 0xFF9F6E},   // pink / orange sunset
            {0x080C24, 0x2B2145},   // night sky
            {0x2E7BD6, 0xBCDDFF},   // clear blue
    };

    public final Setting.Mode preset = add(new Setting.Mode("Sky", "Sun rays", "Storm", "Sunset", "Night", "Clear blue"));
    public final Setting.Bool fog = add(new Setting.Bool("Match horizon / fog", true));

    public SkyChanger() {
        super("SkyChanger", "Photo-style skies (switch in settings)", Category.CLIENT);
        INSTANCE = this;
    }

    /** ARGB sky-dome colour or 0 when off. */
    public static int skyArgb() {
        SkyChanger s = INSTANCE;
        return s == null || !s.enabled ? 0 : 0xFF000000 | PRESETS[s.preset.value][0];
    }

    /** {r,g,b} in 0..1 for the horizon / fog, or null when off. */
    public static float[] horizon() {
        SkyChanger s = INSTANCE;
        if (s == null || !s.enabled || !s.fog.value) return null;
        int c = PRESETS[s.preset.value][1];
        return new float[]{((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f};
    }
}

package dev.xclient.modules;

import com.google.gson.JsonObject;
import dev.xclient.core.*;
import dev.xclient.core.Module;
import dev.xclient.xray.OreFeatures;
import dev.xclient.xray.OreFeatures.Feature;
import dev.xclient.xray.OreSim.Rng;
import dev.xclient.xray.XRayEngine;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.block.Blocks;

/**
 * X-ray that does not depend on what the server sends: ores are computed from the world seed
 * with the vanilla ore feature algorithm (Overworld only).
 */
public class SeedXRay extends Module {
    public final Setting.Str seed = add(new Setting.Str("Seed", ""));
    public final Setting.Blocks blocks = add(new Setting.Blocks("Blocks", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE));
    public final Setting.Num radius = add(new Setting.Num("Range (chunks)", 4, 1, 8, 1));
    public final Setting.Num maxDist = add(new Setting.Num("Max distance", 96, 16, 256, 8));
    public final Setting.Action calibrate = add(new Setting.Action("Calibrate (stand near caves)", () -> XRayEngine.I.requestCalibration()));
    public final Setting.Info status = add(new Setting.Info("Status", () -> XRayEngine.I.status));

    public SeedXRay() { super("SeedXRay", "Seed-based ore finder (ignores server anti-xray)", Category.RENDER); }

    @Override public void onEnable() { XRayEngine.I.reset(); }
    @Override public void onDisable() { XRayEngine.I.reset(); }
    @Override public void onTick() { XRayEngine.I.tick(mc, this); }
    @Override public void onRender(GuiGraphicsExtractor g, float td) { XRayEngine.I.render(g, mc, this); }

    @Override
    public void save(JsonObject o) {
        JsonObject idx = new JsonObject();
        for (Feature f : OreFeatures.ALL) idx.addProperty(f.name, f.index);
        o.add("indices", idx);
        o.addProperty("rng", XRayEngine.I.rng.name());
    }

    @Override
    public void load(JsonObject o) {
        if (o.has("indices")) {
            JsonObject idx = o.getAsJsonObject("indices");
            for (Feature f : OreFeatures.ALL) if (idx.has(f.name)) f.index = idx.get(f.name).getAsInt();
        }
        if (o.has("rng")) {
            try { XRayEngine.I.rng = Rng.valueOf(o.get("rng").getAsString()); } catch (Exception ignored) {}
        }
    }
}

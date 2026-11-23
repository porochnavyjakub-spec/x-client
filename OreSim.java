package dev.xclient.modules;

import java.util.List;

/** CS2-style knife skins. Edit names / numbers here. Textures: assets/xclient/textures/item/<id>.png */
public final class Knives {
    public record Knife(String id, String name, String finish, int catalog, int pattern, String wear,
                        String exterior, int color, String desc) {}

    public static final List<Knife> ALL = List.of(
            new Knife("karambit_doppler", "★ Karambit | Doppler", "Anodized Multicolored", 418, 1, "0.006912482",
                    "Factory New", 0xFF3FB6FF, "Sapphire phase. Serial plate E4-0001/5000."),
            new Knife("nomad_doppler", "★ Nomad Knife | Doppler", "Anodized Multicolored", 421, 991, "0.013668725",
                    "Factory New", 0xFF6C8BFF, "Painted with black and silver metallic, then candy coated."),
            new Knife("butterfly_damascus", "★ Butterfly Knife | Damascus Steel", "Hardened Steel", 410, 577, "0.071230944",
                    "Minimal Wear", 0xFFD9DCE6, "Folded steel with a swirling light and dark pattern."),
            new Knife("talon_doppler", "★ Talon Knife | Doppler", "Anodized Multicolored", 418, 422, "0.009301177",
                    "Factory New", 0xFFA48BFF, "Bone handle inserts and a candy coated blade."),
            new Knife("stiletto_doppler", "★ Stiletto Knife | Doppler", "Anodized Multicolored", 418, 87, "0.011570341",
                    "Factory New", 0xFF9A7BFF, "Slim blade with wooden grips and anodized bolsters."),
            new Knife("m9_bayonet_doppler", "★ M9 Bayonet | Doppler", "Anodized Multicolored", 418, 612, "0.004418870",
                    "Factory New", 0xFF5A5BFF, "Sapphire coloured blade with a serrated spine.")
    );

    public static String[] labels() {
        return ALL.stream().map(Knife::name).toArray(String[]::new);
    }

    private Knives() {}
}

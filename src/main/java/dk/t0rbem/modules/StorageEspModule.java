package dk.t0rbem.modules;

import dk.t0rbem.render.Projector;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2601;
import net.minecraft.class_2609;
import net.minecraft.class_2611;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_2646;
import net.minecraft.class_2818;
import net.minecraft.class_332;
import net.minecraft.class_3719;
import net.minecraft.class_746;

/** Viser kister, tønder, shulkers m.m. gennem vægge i de chunks din klient har indlæst. */
public class StorageEspModule extends Module {
    private final BoolSetting chests = add(new BoolSetting("Chests", true));
    private final BoolSetting enderChests = add(new BoolSetting("Ender chests", true));
    private final BoolSetting barrels = add(new BoolSetting("Barrels", true));
    private final BoolSetting shulkers = add(new BoolSetting("Shulker boxes", true));
    private final BoolSetting furnaces = add(new BoolSetting("Furnaces", false));
    private final BoolSetting hoppers = add(new BoolSetting("Hoppers", false));
    private final BoolSetting dispensers = add(new BoolSetting("Dispensers/droppers", false));
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final BoolSetting names = add(new BoolSetting("Names", false));
    private final NumberSetting range = add(new NumberSetting("Range", 64, 16, 160, 8));

    private final Projector proj = new Projector();

    public StorageEspModule() {
        super("Storage ESP", "Viser kister, tønder, shulkers m.m. gennem vægge.", Category.RENDER, false);
    }

    private String nameFor(class_2586 be) {
        if (be instanceof class_2646) return chests.get() ? "Trapped chest" : null;
        if (be instanceof class_2595) return chests.get() ? "Chest" : null;
        if (be instanceof class_2611) return enderChests.get() ? "Ender chest" : null;
        if (be instanceof class_3719) return barrels.get() ? "Barrel" : null;
        if (be instanceof class_2627) return shulkers.get() ? "Shulker" : null;
        if (be instanceof class_2609) return furnaces.get() ? "Furnace" : null;
        if (be instanceof class_2614) return hoppers.get() ? "Hopper" : null;
        if (be instanceof class_2601) return dispensers.get() ? "Dispenser" : null;
        return null;
    }

    private static int colorFor(String name) {
        return switch (name) {
            case "Chest" -> 0xFFFFAA00;
            case "Trapped chest" -> 0xFFFF5555;
            case "Ender chest" -> 0xFFAA55FF;
            case "Barrel" -> 0xFFCC8844;
            case "Shulker" -> 0xFFFF77CC;
            case "Furnace" -> 0xFFCCCCCC;
            case "Hopper" -> 0xFF888888;
            default -> 0xFF99AACC;
        };
    }

    @Override
    public void renderHud(class_332 ctx, float pt) {
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1687 == null) return;
        proj.update(ctx, pt);

        int pcx = ((int) Math.floor(p.method_23317())) >> 4;
        int pcz = ((int) Math.floor(p.method_23321())) >> 4;
        int rc = (int) Math.ceil(range.get() / 16.0);
        double maxSq = range.get() * range.get();

        for (int cx = pcx - rc; cx <= pcx + rc; cx++) {
            for (int cz = pcz - rc; cz <= pcz + rc; cz++) {
                class_2818 chunk = mc.field_1687.method_2935().method_21730(cx, cz);
                if (chunk == null) continue;

                for (class_2586 be : chunk.method_12214().values()) {
                    String name = nameFor(be);
                    if (name == null) continue;

                    class_2338 bp = be.method_11016();
                    double dx = bp.method_10263() + 0.5 - proj.ex;
                    double dy = bp.method_10264() + 0.5 - proj.ey;
                    double dz = bp.method_10260() + 0.5 - proj.ez;
                    if (dx * dx + dy * dy + dz * dz > maxSq) continue;

                    int[] b = proj.bounds(bp.method_10263(), bp.method_10264(), bp.method_10260(), bp.method_10263() + 1, bp.method_10264() + 1, bp.method_10260() + 1);
                    if (b == null) continue;

                    int color = colorFor(name);
                    Projector.box(ctx, b, color, fill.get());
                    if (names.get()) {
                        int tx = (b[0] + b[2]) / 2 - mc.field_1772.method_1727(name) / 2;
                        ctx.method_25303(mc.field_1772, name, tx, b[1] - 10, color);
                    }
                }
            }
        }
    }
}

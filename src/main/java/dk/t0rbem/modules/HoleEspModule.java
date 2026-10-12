package dk.t0rbem.modules;

import dk.t0rbem.render.Projector;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_332;
import net.minecraft.class_746;

/**
 * Finder 1x1-huller/skakter: lodrette søjler af luft med fast blok på alle fire sider.
 * Scanner én lodret skive pr. tick, så det ikke laver lag.
 * "Min depth" = 1 finder også enkelt-blok-huller (fx PvP-huller).
 */
public class HoleEspModule extends Module {
    private record Hole(int x, int y, int z, int len) {}

    private final NumberSetting minDepth = add(new NumberSetting("Min depth", 3, 1, 12, 1));
    private final NumberSetting range = add(new NumberSetting("Range", 20, 8, 32, 2));
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final BoolSetting labels = add(new BoolSetting("Depth labels", true));

    private final Projector proj = new Projector();
    private final class_2338.class_2339 pos = new class_2338.class_2339();

    private List<Hole> holes = List.of();
    private final List<Hole> building = new ArrayList<>();
    private int slice, cx, cy, cz, r;

    public HoleEspModule() {
        super("Hole ESP", "Viser 1x1-huller og skakter gennem vægge.", Category.RENDER, false);
    }

    @Override
    public void onDisable() {
        holes = List.of();
        building.clear();
        slice = 0;
    }

    @Override
    public void onTick() {
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1687 == null) return;

        if (slice == 0) {
            building.clear();
            cx = (int) Math.floor(p.method_23317());
            cy = (int) Math.floor(p.method_23318());
            cz = (int) Math.floor(p.method_23321());
            r = range.getInt();
        }

        int x = cx - r + slice;
        int bottom = mc.field_1687.method_31607();
        int top = bottom + mc.field_1687.method_31605() - 1;
        int minY = Math.max(bottom, cy - r);
        int maxY = Math.min(top, cy + r);
        int min = minDepth.getInt();

        for (int z = cz - r; z <= cz + r; z++) {
            int run = 0, start = 0;
            for (int y = minY; y <= maxY; y++) {
                if (isShaftCell(x, y, z)) {
                    if (run == 0) start = y;
                    run++;
                } else {
                    if (run >= min) building.add(new Hole(x, start, z, run));
                    run = 0;
                }
            }
            if (run >= min) building.add(new Hole(x, start, z, run));
        }

        slice++;
        if (slice > r * 2) {
            holes = new ArrayList<>(building);
            slice = 0;
        }
    }

    private boolean isShaftCell(int x, int y, int z) {
        pos.method_10103(x, y, z);
        if (!mc.field_1687.method_8320(pos).method_26215()) return false;
        return solid(x + 1, y, z) && solid(x - 1, y, z) && solid(x, y, z + 1) && solid(x, y, z - 1);
    }

    private boolean solid(int x, int y, int z) {
        pos.method_10103(x, y, z);
        class_2680 s = mc.field_1687.method_8320(pos);
        return s.method_26212(mc.field_1687, pos);
    }

    @Override
    public void renderHud(class_332 ctx, float pt) {
        if (mc.field_1724 == null) return;
        proj.update(ctx, pt);

        int drawn = 0;
        for (Hole h : holes) {
            if (drawn >= 200) break;
            int[] b = proj.bounds(h.x(), h.y(), h.z(), h.x() + 1, h.y() + h.len(), h.z() + 1);
            if (b == null) continue;
            drawn++;

            int color = 0xFFFFCC33;
            Projector.box(ctx, b, color, fill.get());
            if (labels.get()) {
                String text = h.len() + " deep";
                int tx = (b[0] + b[2]) / 2 - mc.field_1772.method_1727(text) / 2;
                ctx.method_25303(mc.field_1772, text, tx, b[1] - 10, color);
            }
        }
    }
}

package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_746;

/**
 * 2D-ESP: beregner selv hvor entiteter er på skærmen og tegner en ramme om dem.
 * Virker gennem vægge og kræver ingen mixins.
 */
public class EspModule extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive mobs", false));
    private final BoolSetting items = add(new BoolSetting("Items", false));
    private final BoolSetting names = add(new BoolSetting("Names", true));
    private final NumberSetting range = add(new NumberSetting("Range", 96, 16, 256, 8));

    // kameraets tilstand i den aktuelle frame
    private double ex, ey, ez, fx, fy, fz, rx, ry, rz, ux, uy, uz;
    private double tanHalf, aspect;
    private int sw, sh;

    public EspModule() {
        super("ESP", "Viser spillere og mobs gennem vægge med bokse.", Category.RENDER, false);
    }

    @Override
    public void renderHud(class_332 ctx, float pt) {
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1687 == null) return;

        // kamera
        double eyeOffset = p.method_23320() - p.method_23318();
        ex = class_3532.method_16436(pt, p.field_6038, p.method_23317());
        ey = class_3532.method_16436(pt, p.field_5971, p.method_23318()) + eyeOffset;
        ez = class_3532.method_16436(pt, p.field_5989, p.method_23321());

        double yaw = Math.toRadians(p.method_5705(pt));
        double pitch = Math.toRadians(p.method_5695(pt));
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0;
        rz = -Math.sin(yaw);
        ux = -rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy;

        tanHalf = Math.tan(Math.toRadians(mc.field_1690.method_41808().method_41753()) / 2.0);
        aspect = (double) mc.method_22683().method_4489() / Math.max(1, mc.method_22683().method_4506());
        sw = ctx.method_51421();
        sh = ctx.method_51443();

        double maxSq = range.get() * range.get();

        for (class_1297 e : mc.field_1687.method_18112()) {
            if (e == p) continue;
            int color = colorFor(e);
            if (color == 0) continue;

            double x = class_3532.method_16436(pt, e.field_6038, e.method_23317());
            double y = class_3532.method_16436(pt, e.field_5971, e.method_23318());
            double z = class_3532.method_16436(pt, e.field_5989, e.method_23321());

            double dx = x - ex, dy = y - ey, dz = z - ez;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxSq) continue;

            drawBox(ctx, e, x, y, z, color, Math.sqrt(distSq));
        }
    }

    private int colorFor(class_1297 e) {
        if (e instanceof class_1657) return players.get() ? 0xFF55FFFF : 0;
        if (e instanceof class_1569) return hostile.get() ? 0xFFFF5555 : 0;
        if (e instanceof class_1296) return passive.get() ? 0xFF55FF55 : 0;
        if (e instanceof class_1542) return items.get() ? 0xFFFFFF55 : 0;
        return 0;
    }

    private void drawBox(class_332 ctx, class_1297 e, double x, double y, double z, int color, double dist) {
        double hw = e.method_17681() / 2.0, h = e.method_17682();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (int i = 0; i < 8; i++) {
            double cx = x + ((i & 1) == 0 ? -hw : hw);
            double cy = y + ((i & 2) == 0 ? 0 : h);
            double cz = z + ((i & 4) == 0 ? -hw : hw);
            double[] s = project(cx, cy, cz);
            if (s == null) return; // et hjørne er bag kameraet
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }

        int x1 = (int) minX, y1 = (int) minY, x2 = (int) maxX, y2 = (int) maxY;
        if (x2 < 0 || y2 < 0 || x1 > sw || y1 > sh) return;

        ctx.method_25294(x1, y1, x2, y1 + 1, color);
        ctx.method_25294(x1, y2 - 1, x2, y2, color);
        ctx.method_25294(x1, y1, x1 + 1, y2, color);
        ctx.method_25294(x2 - 1, y1, x2, y2, color);

        if (names.get()) {
            String name = e instanceof class_1542 item
                    ? item.method_6983().method_7964().getString()
                    : e.method_5477().getString();
            String label = name + " " + (int) dist + "m";
            int tx = (x1 + x2) / 2 - mc.field_1772.method_1727(label) / 2;
            ctx.method_25303(mc.field_1772, label, tx, y1 - 10, color);
        }
    }

    /** Verden -> skærm (GUI-koordinater). Returnerer null hvis punktet er bag kameraet. */
    private double[] project(double x, double y, double z) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth < 0.1) return null;
        double xn = (dx * rx + dy * ry + dz * rz) / depth / (tanHalf * aspect);
        double yn = (dx * ux + dy * uy + dz * uz) / depth / tanHalf;
        return new double[]{(xn + 1.0) / 2.0 * sw, (1.0 - yn) / 2.0 * sh};
    }
}

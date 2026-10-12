package dk.t0rbem.render;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_746;

/**
 * Regner verdenskoordinater om til skærmkoordinater (GUI-skala), så moduler kan tegne
 * bokse oven på verdenen uden mixins. Kald update() én gang pr. frame.
 */
public class Projector {
    public double ex, ey, ez;
    public int sw, sh;
    private double fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;

    public void update(class_332 ctx, float pt) {
        class_310 mc = class_310.method_1551();
        class_746 p = mc.field_1724;
        if (p == null) return;

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
    }

    /** Punkt -> {x, y} på skærmen, eller null hvis det er bag kameraet. */
    public double[] project(double x, double y, double z) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth < 0.1) return null;
        double xn = (dx * rx + dy * ry + dz * rz) / depth / (tanHalf * aspect);
        double yn = (dx * ux + dy * uy + dz * uz) / depth / tanHalf;
        return new double[]{(xn + 1.0) / 2.0 * sw, (1.0 - yn) / 2.0 * sh};
    }

    /** Skærmrektangel {minX, minY, maxX, maxY} om en boks i verden, eller null hvis den ikke er synlig. */
    public int[] bounds(double x1, double y1, double z1, double x2, double y2, double z2) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double[] s = project((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2);
            if (s == null) return null; // et hjørne er bag kameraet
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }
        if (maxX < 0 || maxY < 0 || minX > sw || minY > sh) return null;
        return new int[]{
                (int) Math.max(-50, minX), (int) Math.max(-50, minY),
                (int) Math.min(sw + 50, maxX), (int) Math.min(sh + 50, maxY)
        };
    }

    /** Tegner en 1 px linje mellem to skærmpunkter (fra project()). Null = bag kameraet, så springes over. */
    public void line(class_332 ctx, double[] a, double[] b, int color) {
        if (a == null || b == null) return;
        if (Math.max(Math.abs(a[0]), Math.abs(a[1])) > 5000 || Math.max(Math.abs(b[0]), Math.abs(b[1])) > 5000) return;
        double dx = b[0] - a[0], dy = b[1] - a[1];
        int n = (int) Math.min(8000, Math.max(Math.abs(dx), Math.abs(dy)));
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : (double) i / n;
            int px = (int) (a[0] + dx * t), py = (int) (a[1] + dy * t);
            if (px < 0 || py < 0 || px >= sw || py >= sh) continue;
            ctx.method_25294(px, py, px + 1, py + 1, color);
        }
    }

    /** Tegner alle 12 kanter af en boks i verden som en 3D-wireframe. */
    public void wireBox(class_332 ctx, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        double[][] c = new double[8][];
        for (int i = 0; i < 8; i++) {
            c[i] = project((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2);
        }
        for (int i = 0; i < 8; i++) {
            for (int bit = 1; bit <= 4; bit <<= 1) {
                if ((i & bit) == 0) line(ctx, c[i], c[i | bit], color);
            }
        }
    }

    public static void box(class_332 ctx, int[] b, int color, boolean fill) {
        int x1 = b[0], y1 = b[1], x2 = b[2], y2 = b[3];
        if (fill) ctx.method_25294(x1, y1, x2, y2, (color & 0x00FFFFFF) | 0x33000000);
        ctx.method_25294(x1, y1, x2, y1 + 1, color);
        ctx.method_25294(x1, y2 - 1, x2, y2, color);
        ctx.method_25294(x1, y1, x1 + 1, y2, color);
        ctx.method_25294(x2 - 1, y1, x2, y2, color);
    }
}

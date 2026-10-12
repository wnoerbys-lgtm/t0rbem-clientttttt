package dk.t0rbem.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;

/**
 * Regner verdenskoordinater om til skærmkoordinater (GUI-skala), så moduler kan tegne
 * bokse oven på verdenen uden mixins. Kald update() én gang pr. frame.
 *
 * 2D-bokse: bounds() + box(). 3D-wireframe: box3d() / line().
 */
public class Projector {
    /** Kanter bag dette dybdeplan klippes væk (lidt over 0.1, så project() ikke afviser dem). */
    private static final double NEAR_CLIP = 0.11;

    public double ex, ey, ez;
    public int sw, sh;
    private double fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;

    public void update(DrawContext ctx, float pt) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Kameraets entitet (Freecam) – ellers spilleren – så overlays følger det du faktisk ser
        Entity p = mc.getCameraEntity();
        if (p == null) p = mc.player;
        if (p == null) return;

        double eyeOffset = p.getEyeY() - p.getY();
        ex = MathHelper.lerp(pt, p.lastRenderX, p.getX());
        ey = MathHelper.lerp(pt, p.lastRenderY, p.getY()) + eyeOffset;
        ez = MathHelper.lerp(pt, p.lastRenderZ, p.getZ());

        double yaw = Math.toRadians(p.getYaw(pt));
        double pitch = Math.toRadians(p.getPitch(pt));
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0;
        rz = -Math.sin(yaw);
        ux = -rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy;

        tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        aspect = (double) mc.getWindow().getFramebufferWidth() / Math.max(1, mc.getWindow().getFramebufferHeight());
        sw = ctx.getScaledWindowWidth();
        sh = ctx.getScaledWindowHeight();
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

    public static void box(DrawContext ctx, int[] b, int color, boolean fill) {
        int x1 = b[0], y1 = b[1], x2 = b[2], y2 = b[3];
        if (fill) ctx.fill(x1, y1, x2, y2, (color & 0x00FFFFFF) | 0x33000000);
        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);
    }

    // ---------------------------------------------------------------- 3D

    private double depth(double x, double y, double z) {
        return (x - ex) * fx + (y - ey) * fy + (z - ez) * fz;
    }

    /** Er mindst ét hjørne af boksen foran kameraet? (billigt tjek før box3d) */
    public boolean visible(double x1, double y1, double z1, double x2, double y2, double z2) {
        for (int i = 0; i < 8; i++) {
            if (depth((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2) >= NEAR_CLIP) return true;
        }
        return false;
    }

    /** Tegner de 12 kanter af en boks som rigtig 3D-wireframe (kanter klippes mod kameraet og skærmen). */
    public void box3d(DrawContext ctx, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        for (int i = 0; i < 8; i++) {
            for (int bit = 1; bit <= 4; bit <<= 1) {
                if ((i & bit) != 0) continue;
                int j = i | bit;
                line(ctx,
                        (i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2,
                        (j & 1) == 0 ? x1 : x2, (j & 2) == 0 ? y1 : y2, (j & 4) == 0 ? z1 : z2,
                        color);
            }
        }
    }

    /** Linje mellem to verdenspunkter. Klippes mod kameraets nær-plan, så den også virker når den ene ende er bag dig. */
    public void line(DrawContext ctx, double ax, double ay, double az, double bx, double by, double bz, int color) {
        double da = depth(ax, ay, az), db = depth(bx, by, bz);
        if (da < NEAR_CLIP && db < NEAR_CLIP) return;
        if (da < NEAR_CLIP) {
            double t = (NEAR_CLIP - da) / (db - da);
            ax += (bx - ax) * t;
            ay += (by - ay) * t;
            az += (bz - az) * t;
        } else if (db < NEAR_CLIP) {
            double t = (NEAR_CLIP - db) / (da - db);
            bx += (ax - bx) * t;
            by += (ay - by) * t;
            bz += (az - bz) * t;
        }
        double[] a = project(ax, ay, az), b = project(bx, by, bz);
        if (a == null || b == null) return;
        line2d(ctx, a[0], a[1], b[0], b[1], color);
    }

    /** 2D-linje i skærmkoordinater, klippet til skærmen (Liang–Barsky) og tegnet med Bresenham. */
    private void line2d(DrawContext ctx, double x0, double y0, double x1, double y1, int color) {
        double dx = x1 - x0, dy = y1 - y0;
        double t0 = 0, t1 = 1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x0, sw - 1 - x0, y0, sh - 1 - y0};
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return;
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) {
                    if (r > t1) return;
                    if (r > t0) t0 = r;
                } else {
                    if (r < t0) return;
                    if (r < t1) t1 = r;
                }
            }
        }
        bresenham(ctx,
                (int) Math.round(x0 + t0 * dx), (int) Math.round(y0 + t0 * dy),
                (int) Math.round(x0 + t1 * dx), (int) Math.round(y0 + t1 * dy), color);
    }

    /** Bresenham, men pixels i samme række/kolonne samles til ét fill-kald (langt hurtigere). */
    private static void bresenham(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        if (dx >= dy) {
            int err = dx / 2, y = y0, start = x0;
            for (int x = x0; ; x += sx) {
                err -= dy;
                if (x == x1) {
                    run(ctx, start, x, y, true, color);
                    break;
                }
                if (err < 0) {
                    run(ctx, start, x, y, true, color);
                    start = x + sx;
                    y += sy;
                    err += dx;
                }
            }
        } else {
            int err = dy / 2, x = x0, start = y0;
            for (int y = y0; ; y += sy) {
                err -= dx;
                if (y == y1) {
                    run(ctx, start, y, x, false, color);
                    break;
                }
                if (err < 0) {
                    run(ctx, start, y, x, false, color);
                    start = y + sy;
                    x += sx;
                    err += dy;
                }
            }
        }
    }

    private static void run(DrawContext ctx, int a, int b, int c, boolean horizontal, int color) {
        int lo = Math.min(a, b), hi = Math.max(a, b);
        if (horizontal) ctx.fill(lo, c, hi + 1, c + 1, color);
        else ctx.fill(c, lo, c + 1, hi + 1, color);
    }
}

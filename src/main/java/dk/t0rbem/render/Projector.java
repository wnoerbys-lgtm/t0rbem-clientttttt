package dk.t0rbem.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Regner verdenskoordinater om til skærmkoordinater (GUI-skala), så moduler kan tegne
 * bokse oven på verdenen uden mixins. Kald update() én gang pr. frame.
 */
public class Projector {
    public double ex, ey, ez;
    public int sw, sh;
    private double fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;

    public void update(DrawContext ctx, float pt) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
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
}

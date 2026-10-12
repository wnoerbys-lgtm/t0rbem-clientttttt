package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

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
    public void renderHud(DrawContext ctx, float pt) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        // kamera
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

        double maxSq = range.get() * range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == p) continue;
            int color = colorFor(e);
            if (color == 0) continue;

            double x = MathHelper.lerp(pt, e.lastRenderX, e.getX());
            double y = MathHelper.lerp(pt, e.lastRenderY, e.getY());
            double z = MathHelper.lerp(pt, e.lastRenderZ, e.getZ());

            double dx = x - ex, dy = y - ey, dz = z - ez;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxSq) continue;

            drawBox(ctx, e, x, y, z, color, Math.sqrt(distSq));
        }
    }

    private int colorFor(Entity e) {
        if (e instanceof PlayerEntity) return players.get() ? 0xFF55FFFF : 0;
        if (e instanceof Monster) return hostile.get() ? 0xFFFF5555 : 0;
        if (e instanceof PassiveEntity) return passive.get() ? 0xFF55FF55 : 0;
        if (e instanceof ItemEntity) return items.get() ? 0xFFFFFF55 : 0;
        return 0;
    }

    private void drawBox(DrawContext ctx, Entity e, double x, double y, double z, int color, double dist) {
        double hw = e.getWidth() / 2.0, h = e.getHeight();
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

        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);

        if (names.get()) {
            String name = e instanceof ItemEntity item
                    ? item.getStack().getName().getString()
                    : e.getName().getString();
            String label = name + " " + (int) dist + "m";
            int tx = (x1 + x2) / 2 - mc.textRenderer.getWidth(label) / 2;
            ctx.drawTextWithShadow(mc.textRenderer, label, tx, y1 - 10, color);
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

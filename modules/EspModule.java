package dk.t0rbem.modules;

import dk.t0rbem.render.Projector;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.ModeSetting;
import dk.t0rbem.settings.NumberSetting;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * ESP: beregner selv hvor entiteter er på skærmen (via Projector) og tegner dem gennem vægge.
 * Style "3D" tegner en rigtig wireframe-boks i verden, "2D" en flad ramme. Kræver ingen mixins.
 */
public class EspModule extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "3D", "2D", "3D"));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive mobs", false));
    private final BoolSetting items = add(new BoolSetting("Items", false));
    private final BoolSetting names = add(new BoolSetting("Names", true));
    private final NumberSetting range = add(new NumberSetting("Range", 96, 16, 256, 8));

    private final Projector proj = new Projector();

    public EspModule() {
        super("ESP", "Viser spillere og mobs gennem vægge med bokse (2D eller 3D).", Category.RENDER, false);
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        proj.update(ctx, pt);

        boolean threeD = style.index() == 1;
        double maxSq = range.get() * range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == p) continue;
            int color = colorFor(e);
            if (color == 0) continue;

            double x = MathHelper.lerp(pt, e.lastRenderX, e.getX());
            double y = MathHelper.lerp(pt, e.lastRenderY, e.getY());
            double z = MathHelper.lerp(pt, e.lastRenderZ, e.getZ());

            double dx = x - proj.ex, dy = y - proj.ey, dz = z - proj.ez;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxSq) continue;

            double hw = e.getWidth() / 2.0, h = e.getHeight();
            String label = names.get() ? labelFor(e, Math.sqrt(distSq)) : null;

            if (threeD) {
                if (!proj.visible(x - hw, y, z - hw, x + hw, y + h, z + hw)) continue;
                proj.box3d(ctx, x - hw, y, z - hw, x + hw, y + h, z + hw, color);
                if (label != null) {
                    double[] s = proj.project(x, y + h + 0.15, z);
                    if (s != null) drawLabel(ctx, label, (int) s[0], (int) s[1] - 10, color);
                }
            } else {
                int[] b = proj.bounds(x - hw, y, z - hw, x + hw, y + h, z + hw);
                if (b == null) continue;
                Projector.box(ctx, b, color, false);
                if (label != null) drawLabel(ctx, label, (b[0] + b[2]) / 2, b[1] - 10, color);
            }
        }
    }

    private int colorFor(Entity e) {
        if (e instanceof PlayerEntity) return players.get() ? 0xFF55FFFF : 0;
        if (e instanceof Monster) return hostile.get() ? 0xFFFF5555 : 0;
        if (e instanceof PassiveEntity) return passive.get() ? 0xFF55FF55 : 0;
        if (e instanceof ItemEntity) return items.get() ? 0xFFFFFF55 : 0;
        return 0;
    }

    private String labelFor(Entity e, double dist) {
        String name = e instanceof ItemEntity item
                ? item.getStack().getName().getString()
                : e.getName().getString();
        return name + " " + (int) dist + "m";
    }

    /** Tegner tekst centreret om x. */
    private void drawLabel(DrawContext ctx, String text, int cx, int y, int color) {
        ctx.drawTextWithShadow(mc.textRenderer, text, cx - mc.textRenderer.getWidth(text) / 2, y, color);
    }
}

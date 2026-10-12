package dk.t0rbem.modules;

import dk.t0rbem.render.Projector;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Fælles base for chunk-findere: subklassen finder kandidat-chunks (collect), basen filtrerer på
 * afstand, tager de nærmeste og tegner dem som 3D-bokse i verdenen med koordinater.
 */
public abstract class ChunkFinderModule extends Module {
    /** slab = ingen højdeinfo, så der tegnes en flad boks i din egen højde. */
    protected record Target(int cx, int cz, int minY, int maxY, boolean slab, String detail) {}

    protected final BoolSetting names = add(new BoolSetting("Names", true));
    protected final NumberSetting showRange = add(new NumberSetting("Show range", 1000, 128, 5000, 64));
    protected final NumberSetting maxBoxes = add(new NumberSetting("Max boxes", 60, 10, 300, 10));

    private final Projector proj = new Projector();
    private List<Target> targets = List.of();
    private int timer;

    protected ChunkFinderModule(String name, String description) {
        super(name, description, Category.RENDER, false);
    }

    protected abstract int color();
    protected abstract String title();

    /** Fyld out med alle kendte kandidater. Kaldes ca. hvert sekund. */
    protected abstract void collect(List<Target> out);

    /** Kaldes hver tick (fx til at tælle tid i chunks). */
    protected void perTick() {}

    @Override
    public void onTick() {
        perTick();
        if (timer-- > 0) return;
        timer = 20;
        refresh();
    }

    @Override
    public void onDisable() {
        targets = List.of();
        timer = 0;
    }

    private void refresh() {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        List<Target> all = new ArrayList<>();
        collect(all);

        double px = p.getX(), pz = p.getZ();
        double max = showRange.get();
        double maxSq = max * max;
        all.removeIf(t -> distSq(t, px, pz) > maxSq);
        all.sort(Comparator.comparingDouble(t -> distSq(t, px, pz)));
        int cap = maxBoxes.getInt();
        targets = all.size() > cap ? new ArrayList<>(all.subList(0, cap)) : all;
    }

    private static double distSq(Target t, double px, double pz) {
        double dx = t.cx() * 16 + 8 - px, dz = t.cz() * 16 + 8 - pz;
        return dx * dx + dz * dz;
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || targets.isEmpty()) return;
        proj.update(ctx, pt);

        int color = color();
        double feet = Math.floor(proj.ey - 1.62);

        for (Target t : targets) {
            double x1 = t.cx() * 16.0, z1 = t.cz() * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            double y1 = t.slab() ? feet : t.minY();
            double y2 = t.slab() ? feet + 1 : t.maxY() + 1;
            if (!proj.visible(x1, y1, z1, x2, y2, z2)) continue;

            proj.box3d(ctx, x1, y1, z1, x2, y2, z2, color);

            if (names.get()) {
                double[] s = proj.project(x1 + 8, y2 + 0.5, z1 + 8);
                if (s == null) continue;
                int dist = (int) Math.hypot(x1 + 8 - proj.ex, z1 + 8 - proj.ez);
                String l1 = title() + "  " + (int) (x1 + 8) + ", " + (int) (z1 + 8) + "  " + dist + "m";
                int sy = (int) s[1];
                if (t.detail().isEmpty()) {
                    drawCentered(ctx, l1, (int) s[0], sy - 10, color);
                } else {
                    drawCentered(ctx, l1, (int) s[0], sy - 20, color);
                    drawCentered(ctx, t.detail(), (int) s[0], sy - 10, color);
                }
            }
        }
    }

    private void drawCentered(DrawContext ctx, String text, int cx, int y, int color) {
        ctx.drawTextWithShadow(mc.textRenderer, text, cx - mc.textRenderer.getWidth(text) / 2, y, color);
    }
}

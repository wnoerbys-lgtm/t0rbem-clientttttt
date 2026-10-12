package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.Module;
import dk.t0rbem.modules.ModuleManager;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.ModeSetting;
import dk.t0rbem.settings.NumberSetting;
import dk.t0rbem.settings.Setting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Venstreklik = slå modul til/fra. Højreklik = vis/skjul indstillinger.
 * Immediate-mode: alle klikbare områder genopbygges hver frame.
 */
public class ClickGuiScreen extends class_437 {
    private static final int W = 124, ROW = 14, GAP = 8, X0 = 14, Y0 = 14;
    private static final int ACCENT = 0xFF1D8F6A, BG = 0xDD0E1217, HOVER = 0x22FFFFFF;

    private static final Set<Module> EXPANDED = new HashSet<>();

    private static final class Hit {
        final int x, y, w, h;
        Runnable left, right;
        NumberSetting slider;
        int barX, barW;

        Hit(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragHit;
    private String tip;

    public ClickGuiScreen() {
        super(class_2561.method_43470(T0rbemClient.NAME));
    }

    @Override
    public boolean method_25421() { return false; }

    @Override
    public void method_25432() { Config.save(); }

    private Hit addHit(int x, int y, int w, int h) {
        Hit hit = new Hit(x, y, w, h);
        hits.add(hit);
        return hit;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- tegning ----------

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        ctx.method_25294(0, 0, field_22789, field_22790, 0x77000000);
        hits.clear();
        tip = null;

        int px = X0;
        for (Module.Category cat : Module.Category.values()) {
            drawModulePanel(ctx, px, cat, mouseX, mouseY);
            px += W + GAP;
        }
        drawClientPanel(ctx, px, mouseX, mouseY);

        String t = tip != null ? tip : "Venstreklik: til/fra  |  Højreklik: indstillinger";
        ctx.method_25303(field_22793, t, (field_22789 - field_22793.method_1727(t)) / 2, field_22790 - 16, 0xFFCCCCCC);
    }

    private int heightOf(Module m) {
        int h = ROW;
        if (EXPANDED.contains(m)) {
            for (Setting s : m.settings()) h += s instanceof NumberSetting ? 22 : ROW;
        }
        return h;
    }

    private void drawModulePanel(class_332 ctx, int px, Module.Category cat, int mx, int my) {
        List<Module> mods = ModuleManager.in(cat);
        int total = ROW + 2;
        for (Module m : mods) total += heightOf(m);

        ctx.method_25294(px, Y0, px + W, Y0 + total, BG);
        ctx.method_25294(px, Y0, px + W, Y0 + ROW + 2, ACCENT);
        ctx.method_25303(field_22793, cat.label, px + 5, Y0 + 4, 0xFFFFFFFF);

        int y = Y0 + ROW + 2;
        for (Module m : mods) y = drawModule(ctx, px, y, m, mx, my);
    }

    private int drawModule(class_332 ctx, int px, int y, Module m, int mx, int my) {
        boolean hov = in(mx, my, px, y, W, ROW);
        if (m.enabled) ctx.method_25294(px, y, px + W, y + ROW, 0x551D8F6A);
        if (hov) {
            ctx.method_25294(px, y, px + W, y + ROW, HOVER);
            tip = m.description;
        }
        ctx.method_25303(field_22793, m.name, px + 5, y + 3, m.enabled ? 0xFF7CFFB2 : 0xFFBBBBBB);

        boolean has = !m.settings().isEmpty();
        boolean open = EXPANDED.contains(m);
        if (has) {
            String arrow = open ? "v" : ">";
            ctx.method_25303(field_22793, arrow, px + W - field_22793.method_1727(arrow) - 5, y + 3, 0xFF888888);
        }

        Hit h = addHit(px, y, W, ROW);
        h.left = m::toggle;
        if (has) h.right = () -> toggleOpen(m);

        y += ROW;
        if (open) {
            for (Setting s : m.settings()) y = drawSetting(ctx, px, y, s, mx, my);
        }
        return y;
    }

    private int drawSetting(class_332 ctx, int px, int y, Setting s, int mx, int my) {
        int sx = px + 4, sw = W - 4;

        if (s instanceof NumberSetting n) {
            int h = 22;
            ctx.method_25294(sx, y, px + W, y + h, 0x33000000);
            ctx.method_25303(field_22793, n.name, sx + 4, y + 3, 0xFFAAAAAA);
            String v = n.display();
            ctx.method_25303(field_22793, v, px + W - field_22793.method_1727(v) - 4, y + 3, 0xFFFFFFFF);
            int bx = sx + 4, bw = sw - 8;
            ctx.method_25294(bx, y + 15, bx + bw, y + 18, 0xFF333333);
            ctx.method_25294(bx, y + 15, bx + (int) (bw * n.progress()), y + 18, ACCENT);
            Hit hit = addHit(sx, y, sw, h);
            hit.slider = n;
            hit.barX = bx;
            hit.barW = bw;
            return y + h;
        }

        ctx.method_25294(sx, y, px + W, y + ROW, 0x33000000);
        if (in(mx, my, sx, y, sw, ROW)) ctx.method_25294(sx, y, px + W, y + ROW, HOVER);
        ctx.method_25303(field_22793, s.name, sx + 4, y + 3, 0xFFAAAAAA);

        String value;
        int valueColor = 0xFFFFFFFF;
        Runnable action;
        if (s instanceof BoolSetting b) {
            value = b.get() ? "ON" : "OFF";
            valueColor = b.get() ? 0xFF55FF88 : 0xFFFF5555;
            action = b::toggle;
        } else if (s instanceof ModeSetting mo) {
            value = mo.get();
            action = mo::cycle;
        } else {
            return y + ROW;
        }
        ctx.method_25303(field_22793, value, px + W - field_22793.method_1727(value) - 4, y + 3, valueColor);
        addHit(sx, y, sw, ROW).left = action;
        return y + ROW;
    }

    private void drawClientPanel(class_332 ctx, int px, int mx, int my) {
        int rows = 3;
        int total = ROW + 2 + rows * ROW;
        ctx.method_25294(px, Y0, px + W, Y0 + total, BG);
        ctx.method_25294(px, Y0, px + W, Y0 + ROW + 2, ACCENT);
        ctx.method_25303(field_22793, "Client", px + 5, Y0 + 4, 0xFFFFFFFF);

        int y = Y0 + ROW + 2;
        y = clientRow(ctx, px, y, mx, my, "Edit HUD layout", null, 0xFFFFFFFF,
                "Træk HUD-elementerne hen hvor du vil have dem.",
                () -> net.minecraft.class_310.method_1551().method_1507(new HudEditorScreen()));
        y = clientRow(ctx, px, y, mx, my, "HUD",
                T0rbemClient.hudVisible ? "ON" : "OFF",
                T0rbemClient.hudVisible ? 0xFF55FF88 : 0xFFFF5555,
                "Slå hele HUD'en til eller fra.",
                () -> T0rbemClient.hudVisible = !T0rbemClient.hudVisible);
        clientRow(ctx, px, y, mx, my, "Reset HUD positions", null, 0xFFFFFFFF,
                "Sæt alle HUD-elementer tilbage til standardpositionerne.",
                () -> { for (HudModule h : ModuleManager.hud()) h.resetPosition(); });
    }

    private int clientRow(class_332 ctx, int px, int y, int mx, int my,
                          String label, String value, int valueColor, String tooltip, Runnable action) {
        if (in(mx, my, px, y, W, ROW)) {
            ctx.method_25294(px, y, px + W, y + ROW, HOVER);
            tip = tooltip;
        }
        ctx.method_25303(field_22793, label, px + 5, y + 3, 0xFFDDDDDD);
        if (value != null) {
            ctx.method_25303(field_22793, value, px + W - field_22793.method_1727(value) - 5, y + 3, valueColor);
        }
        addHit(px, y, W, ROW).left = action;
        return y + ROW;
    }

    private static void toggleOpen(Module m) {
        if (!EXPANDED.remove(m)) EXPANDED.add(m);
    }

    // ---------- input ----------

    private static void applySlider(Hit h, double mouseX) {
        NumberSetting n = h.slider;
        double p = Math.max(0, Math.min(1, (mouseX - h.barX) / h.barW));
        n.set(n.min + p * (n.max - n.min));
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        double mx = click.comp_4798(), my = click.comp_4799();
        for (Hit h : hits) {
            if (!h.contains(mx, my)) continue;
            if (click.method_74245() == 0) {
                if (h.slider != null) {
                    dragHit = h;
                    applySlider(h, mx);
                } else if (h.left != null) {
                    h.left.run();
                }
            } else if (click.method_74245() == 1 && h.right != null) {
                h.right.run();
            }
            return true;
        }
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        if (dragHit != null) {
            applySlider(dragHit, click.comp_4798());
            return true;
        }
        return super.method_25403(click, offsetX, offsetY);
    }

    @Override
    public boolean method_25406(class_11909 click) {
        dragHit = null;
        return super.method_25406(click);
    }
}

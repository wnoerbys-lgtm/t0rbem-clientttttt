package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.ModuleManager;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/** Træk HUD-elementerne hen hvor du vil have dem. ESC gemmer og lukker. */
public class HudEditorScreen extends class_437 {
    public static boolean active;

    private HudModule dragging;
    private int offX, offY;

    public HudEditorScreen() {
        super(class_2561.method_43470("HUD editor"));
        active = true;
    }

    @Override
    public boolean method_25421() { return false; }

    @Override
    public void method_25432() {
        active = false;
        Config.save();
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        ctx.method_25294(0, 0, field_22789, field_22790, 0x44000000);

        for (HudModule m : ModuleManager.hud()) {
            if (!m.enabled) continue;
            m.render(ctx);
            int w = m.width(), h = m.height();
            if (w <= 0 || h <= 0) continue;
            int c = m == dragging ? 0xFF55FF88 : 0xAAFFFFFF;
            ctx.method_25294(m.x - 1, m.y - 1, m.x + w + 1, m.y, c);
            ctx.method_25294(m.x - 1, m.y + h, m.x + w + 1, m.y + h + 1, c);
            ctx.method_25294(m.x - 1, m.y, m.x, m.y + h, c);
            ctx.method_25294(m.x + w, m.y, m.x + w + 1, m.y + h, c);
        }

        String hint = "Træk i elementerne - ESC gemmer og lukker";
        ctx.method_25303(field_22793, hint, (field_22789 - field_22793.method_1727(hint)) / 2, field_22790 / 2 - 4, 0xFFFFFFFF);
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() == 0) {
            List<HudModule> list = ModuleManager.hud();
            for (int i = list.size() - 1; i >= 0; i--) {
                HudModule m = list.get(i);
                if (m.enabled && m.contains(click.comp_4798(), click.comp_4799())) {
                    dragging = m;
                    offX = (int) (click.comp_4798() - m.x);
                    offY = (int) (click.comp_4799() - m.y);
                    return true;
                }
            }
        }
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        if (dragging != null) {
            dragging.x = (int) (click.comp_4798() - offX);
            dragging.y = (int) (click.comp_4799() - offY);
            return true;
        }
        return super.method_25403(click, offsetX, offsetY);
    }

    @Override
    public boolean method_25406(class_11909 click) {
        dragging = null;
        return super.method_25406(click);
    }
}

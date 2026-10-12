package dk.t0rbem.modules;

import dk.t0rbem.HudEditorScreen;
import java.util.List;
import net.minecraft.class_332;

/** HUD-element der bare viser nogle tekstlinjer. */
public abstract class TextHudModule extends HudModule {
    private static final int LINE = 10, PAD = 2;
    private List<String> cached = List.of();

    protected TextHudModule(String name, String description, boolean enabled, int x, int y) {
        super(name, description, enabled, x, y);
    }

    protected abstract List<String> lines();

    @Override
    protected void prepare() {
        List<String> l = lines();
        if ((l == null || l.isEmpty()) && HudEditorScreen.active) l = List.of("[" + name + "]");
        cached = l == null ? List.of() : l;
    }

    @Override
    public int width() {
        int w = 0;
        for (String s : cached) w = Math.max(w, mc.field_1772.method_1727(s));
        return cached.isEmpty() ? 0 : w + PAD * 2;
    }

    @Override
    public int height() {
        return cached.isEmpty() ? 0 : cached.size() * LINE + PAD + 1;
    }

    @Override
    protected void draw(class_332 ctx, int x, int y) {
        int c = textColor();
        for (int i = 0; i < cached.size(); i++) {
            ctx.method_25303(mc.field_1772, cached.get(i), x + PAD, y + PAD + i * LINE, c);
        }
    }
}

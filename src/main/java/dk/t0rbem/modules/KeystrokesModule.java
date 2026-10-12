package dk.t0rbem.modules;

import net.minecraft.class_304;
import net.minecraft.class_332;

public class KeystrokesModule extends HudModule {
    private static final int S = 20, GAP = 2, SPACE_H = 10;

    public KeystrokesModule() {
        super("Keystrokes", "Viser WASD og mellemrum.", true, 4, 9999);
        settings.remove(background); // tegner sine egne taster
    }

    @Override
    protected boolean wantsBackground() { return false; }

    @Override
    public int width() { return S * 3 + GAP * 2; }

    @Override
    public int height() { return (S + GAP) * 2 + SPACE_H; }

    @Override
    protected void draw(class_332 ctx, int x, int y) {
        key(ctx, x + S + GAP, y, "W", mc.field_1690.field_1894);
        key(ctx, x, y + S + GAP, "A", mc.field_1690.field_1913);
        key(ctx, x + S + GAP, y + S + GAP, "S", mc.field_1690.field_1881);
        key(ctx, x + (S + GAP) * 2, y + S + GAP, "D", mc.field_1690.field_1849);

        int sy = y + (S + GAP) * 2;
        boolean down = mc.field_1690.field_1903.method_1434();
        ctx.method_25294(x, sy, x + width(), sy + SPACE_H, down ? 0xAAFFFFFF : 0x88000000);
        ctx.method_25294(x + 8, sy + 4, x + width() - 8, sy + 6, down ? 0xFF000000 : textColor());
    }

    private void key(class_332 ctx, int x, int y, String label, class_304 key) {
        boolean down = key.method_1434();
        ctx.method_25294(x, y, x + S, y + S, down ? 0xAAFFFFFF : 0x88000000);
        int tx = x + (S - mc.field_1772.method_1727(label)) / 2;
        ctx.method_51433(mc.field_1772, label, tx, y + 6, down ? 0xFF000000 : textColor(), false);
    }
}

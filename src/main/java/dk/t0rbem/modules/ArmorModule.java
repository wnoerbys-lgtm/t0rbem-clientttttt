package dk.t0rbem.modules;

import dk.t0rbem.HudEditorScreen;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_332;

public class ArmorModule extends HudModule {
    private static final class_1304[] SLOTS = {
            class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166
    };
    private static final int ROW = 18;

    public ArmorModule() { super("Armor", "Viser din rustning og holdbarhed.", true, 9999, 60); }

    private boolean hasAny() {
        if (mc.field_1724 == null) return false;
        for (class_1304 s : SLOTS) if (!mc.field_1724.method_6118(s).method_7960()) return true;
        return false;
    }

    @Override
    public int width() {
        return (hasAny() || HudEditorScreen.active) ? 54 : 0;
    }

    @Override
    public int height() {
        return (hasAny() || HudEditorScreen.active) ? ROW * 4 : 0;
    }

    @Override
    protected void draw(class_332 ctx, int x, int y) {
        if (mc.field_1724 == null) return;
        for (int i = 0; i < SLOTS.length; i++) {
            class_1799 stack = mc.field_1724.method_6118(SLOTS[i]);
            if (stack.method_7960()) continue;
            int ry = y + i * ROW;
            ctx.method_51427(stack, x + 1, ry + 1);
            if (stack.method_7963()) {
                int left = stack.method_7936() - stack.method_7919();
                ctx.method_25303(mc.field_1772, String.valueOf(left), x + 20, ry + 5, textColor());
            }
        }
    }
}

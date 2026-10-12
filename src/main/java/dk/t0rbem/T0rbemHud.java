package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.Module;
import dk.t0rbem.modules.ModuleManager;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

public class T0rbemHud {
    public static void render(class_332 ctx, class_9779 tickCounter) {
        class_310 mc = class_310.method_1551();
        if (!T0rbemClient.hudVisible || HudEditorScreen.active) return; // editoren tegner selv
        if (mc.field_1724 == null || mc.field_1690.field_1842) return;

        float pt = tickCounter.method_60637(false);
        for (Module m : ModuleManager.all()) {
            if (m.enabled && !(m instanceof HudModule)) m.renderHud(ctx, pt);
        }

        for (HudModule m : ModuleManager.hud()) {
            if (m.enabled) m.render(ctx);
        }
    }
}

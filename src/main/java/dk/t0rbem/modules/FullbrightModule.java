package dk.t0rbem.modules;

import net.minecraft.class_1293;
import net.minecraft.class_1294;

public class FullbrightModule extends Module {
    public FullbrightModule() {
        super("Fullbright", "Fuld lysstyrke overalt (kun visuelt).", Category.RENDER, false);
    }

    @Override
    public void onTick() {
        if (mc.field_1724 == null) return;
        mc.field_1724.method_6092(new class_1293(class_1294.field_5925, 400, 0, false, false, false));
    }

    @Override
    public void onDisable() {
        if (mc.field_1724 != null) mc.field_1724.method_6016(class_1294.field_5925);
    }
}

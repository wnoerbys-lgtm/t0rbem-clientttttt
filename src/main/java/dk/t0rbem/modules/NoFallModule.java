package dk.t0rbem.modules;

import dk.t0rbem.settings.NumberSetting;
import net.minecraft.class_2828;
import net.minecraft.class_746;

public class NoFallModule extends Module {
    private final NumberSetting minFall = add(new NumberSetting("Min fall distance", 2.5, 1, 10, 0.5));

    public NoFallModule() {
        super("NoFall", "Undgår faldskade ved at fortælle serveren, at du står på jorden.", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        class_746 p = mc.field_1724;
        if (p == null || p.method_24828() || mc.method_1562() == null) return;
        if (p.field_6017 > minFall.get()) {
            mc.method_1562().method_52787(new class_2828.class_5911(true, p.field_5976));
        }
    }
}

package dk.t0rbem.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1293;

public class PotionModule extends TextHudModule {
    public PotionModule() { super("Potions", "Viser aktive effekter og tid tilbage.", true, 9999, 4); }

    @Override
    protected List<String> lines() {
        List<String> out = new ArrayList<>();
        if (mc.field_1724 == null) return out;
        for (class_1293 e : mc.field_1724.method_6026()) {
            String name = e.method_5579().comp_349().method_5560().getString();
            String lvl = e.method_5578() > 0 ? " " + (e.method_5578() + 1) : "";
            out.add(name + lvl + " " + (e.method_48559() ? "inf" : time(e.method_5584())));
        }
        return out;
    }

    private static String time(int ticks) {
        int s = ticks / 20;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }
}

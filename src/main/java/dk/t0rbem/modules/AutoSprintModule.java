package dk.t0rbem.modules;

public class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("Auto Sprint", "Sprinter automatisk når du går fremad.", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        var p = mc.field_1724;
        if (p == null) return;
        if (mc.field_1690.field_1894.method_1434()
                && !p.method_5715()
                && !p.field_5976
                && p.method_7344().method_7586() > 6) {
            p.method_5728(true);
        }
    }
}

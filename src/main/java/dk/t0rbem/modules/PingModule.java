package dk.t0rbem.modules;

import java.util.List;

public class PingModule extends TextHudModule {
    public PingModule() { super("Ping", "Viser din ping til serveren.", true, 4, 36); }

    @Override
    protected List<String> lines() {
        if (mc.field_1724 == null || mc.method_1562() == null) return List.of("Ping: ?");
        var entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
        return List.of("Ping: " + (entry == null ? "?" : entry.method_2959() + " ms"));
    }
}

package dk.t0rbem.modules;

import java.util.List;

public class BiomeModule extends TextHudModule {
    public BiomeModule() { super("Biome", "Viser det biome du står i.", false, 4, 126); }

    @Override
    protected List<String> lines() {
        if (mc.field_1724 == null || mc.field_1687 == null) return List.of();
        String name = mc.field_1687.method_23753(mc.field_1724.method_24515()).method_40230()
                .map(k -> k.method_29177().method_12832().replace('_', ' '))
                .orElse("unknown");
        return List.of("Biome: " + name);
    }
}

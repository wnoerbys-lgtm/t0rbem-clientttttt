package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1937;

public class CoordsModule extends TextHudModule {
    private final NumberSetting decimals = add(new NumberSetting("Decimals", 1, 0, 2, 1));
    private final BoolSetting direction = add(new BoolSetting("Direction", true));
    private final BoolSetting nether = add(new BoolSetting("Nether coords", false));

    public CoordsModule() { super("Coordinates", "Viser XYZ, retning og nether-koordinater.", true, 4, 84); }

    @Override
    protected List<String> lines() {
        if (mc.field_1724 == null || mc.field_1687 == null) return List.of();
        String f = "%." + decimals.getInt() + "f";
        double x = mc.field_1724.method_23317(), y = mc.field_1724.method_23318(), z = mc.field_1724.method_23321();
        List<String> out = new ArrayList<>();
        out.add(String.format(Locale.ROOT, "XYZ: " + f + " / " + f + " / " + f, x, y, z));
        if (direction.get()) out.add("Facing: " + mc.field_1724.method_5735().method_15434());
        if (nether.get()) {
            if (mc.field_1687.method_27983() == class_1937.field_25179) {
                out.add(String.format(Locale.ROOT, "Nether: " + f + " / " + f, x / 8.0, z / 8.0));
            } else if (mc.field_1687.method_27983() == class_1937.field_25180) {
                out.add(String.format(Locale.ROOT, "Overworld: " + f + " / " + f, x * 8.0, z * 8.0));
            }
        }
        return out;
    }
}

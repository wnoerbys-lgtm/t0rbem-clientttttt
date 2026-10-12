package dk.t0rbem.modules;

import dk.t0rbem.settings.NumberSetting;
import net.minecraft.class_746;

public class FlightModule extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 0.1, 3.0, 0.1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical speed", 0.4, 0.1, 2.0, 0.1));

    public FlightModule() {
        super("Flight", "Flyv frit: WASD, Mellemrum op, Shift ned. (G slår til/fra)", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        class_746 p = mc.field_1724;
        if (p == null) return;

        p.method_5875(true);
        if (mc.field_1755 != null) {
            p.method_18800(0, 0, 0);
            return;
        }

        double f = (mc.field_1690.field_1894.method_1434() ? 1 : 0) - (mc.field_1690.field_1881.method_1434() ? 1 : 0);
        double s = (mc.field_1690.field_1849.method_1434() ? 1 : 0) - (mc.field_1690.field_1913.method_1434() ? 1 : 0);

        double yaw = Math.toRadians(p.method_36454());
        double vx = -Math.sin(yaw) * f - Math.cos(yaw) * s;
        double vz = Math.cos(yaw) * f - Math.sin(yaw) * s;
        double len = Math.hypot(vx, vz);
        if (len > 0) {
            vx = vx / len * speed.get();
            vz = vz / len * speed.get();
        }

        double vy = ((mc.field_1690.field_1903.method_1434() ? 1 : 0) - (mc.field_1690.field_1832.method_1434() ? 1 : 0))
                * vertical.get();

        p.method_18800(vx, vy, vz);
    }

    @Override
    public void onDisable() {
        if (mc.field_1724 != null) mc.field_1724.method_5875(false);
    }
}

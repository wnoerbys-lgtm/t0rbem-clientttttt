package dk.t0rbem.modules;

import dk.t0rbem.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;

/**
 * Freecam: kameraet flyver frit rundt, mens din spiller står stille.
 * Selve kameraet flyttes af CameraMixin, musen fanges af EntityMixin og
 * WASD/space/shift til spilleren slås fra af KeyboardInputMixin.
 */
public class FreecamModule extends Module {
    public static FreecamModule INSTANCE;

    private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 0.1, 3.0, 0.1));

    public double x, y, z, prevX, prevY, prevZ;
    public float yaw, pitch;
    private Object level;

    public FreecamModule() {
        super("Freecam", "Flyv kameraet frit rundt, mens din spiller står stille.", Category.RENDER, false);
        INSTANCE = this;
    }

    /** True når freecam er slået til og vi er i en verden. */
    public static boolean isActive() {
        FreecamModule f = INSTANCE;
        Minecraft mc = Minecraft.getInstance();
        return f != null && f.enabled && mc.player != null && mc.level != null;
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            enabled = false;
            return;
        }
        x = prevX = mc.player.getX();
        y = prevY = mc.player.getEyeY();
        z = prevZ = mc.player.getZ();
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        level = mc.level;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) { // skiftet verden/dimension -> slå fra
            setEnabled(false);
            return;
        }

        prevX = x;
        prevY = y;
        prevZ = z;

        Options o = mc.options;
        double fwd = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
        double side = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        double up = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);

        double len = Math.sqrt(fwd * fwd + side * side);
        if (len > 0) {
            fwd /= len;
            side /= len;
        }

        double rad = Math.toRadians(yaw);
        double s = speed.get() * (o.keySprint.isDown() ? 2.0 : 1.0);
        x += (-Math.sin(rad) * fwd + Math.cos(rad) * side) * s;
        z += (Math.cos(rad) * fwd + Math.sin(rad) * side) * s;
        y += up * s;
    }

    /** Kaldes af EntityMixin i stedet for at musen drejer spilleren. */
    public void turn(double yRot, double xRot) {
        yaw += (float) (yRot * 0.15);
        pitch = Mth.clamp(pitch + (float) (xRot * 0.15), -90.0F, 90.0F);
    }

    public double lerpX(float pt) { return prevX + (x - prevX) * pt; }
    public double lerpY(float pt) { return prevY + (y - prevY) * pt; }
    public double lerpZ(float pt) { return prevZ + (z - prevZ) * pt; }
}

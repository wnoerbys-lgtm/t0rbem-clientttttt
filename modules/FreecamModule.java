package dk.t0rbem.modules;

import com.mojang.authlib.GameProfile;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.GameOptions;

/**
 * Freecam: kameraet flyver frit rundt, mens din rigtige spiller står stille.
 *
 * Princip (ingen mixins): vi laver en klientside-"kamera-entitet" og sætter den som kameraets mål
 * med MinecraftClient#setCameraEntity. Den rigtige spillers input erstattes af et tomt Input, så den
 * ikke bevæger sig, og klik (angreb/brug/pick) sluges, så der ikke sendes handlinger til serveren
 * fra kameraets position.
 */
public class FreecamModule extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));
    private final BoolSetting followPitch = add(new BoolSetting("Move where you look", true));

    private OtherClientPlayerEntity cam;
    private ClientPlayerEntity owner;
    private Input savedInput;

    public FreecamModule() {
        super("Freecam", "Flyv kameraet frit rundt mens din spiller står stille. WASD, Mellemrum op, Shift ned.",
                Category.RENDER, false);
        // Køres FØR vanilla håndterer input hvert tick, så klik aldrig når frem til spillet.
        ClientTickEvents.START_CLIENT_TICK.register(client -> swallowClicks());
    }

    @Override
    public void onEnable() {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) {
            enabled = false; // fx når config indlæses før en verden findes
            return;
        }

        // Disse styrer den rigtige spiller og må ikke køre samtidig
        Module flight = ModuleManager.get("Flight");
        if (flight != null) flight.setEnabled(false);
        Module sprint = ModuleManager.get("Auto Sprint");
        if (sprint != null) sprint.setEnabled(false);

        owner = p;
        cam = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), "Freecam"));
        place(p.getX(), p.getY(), p.getZ());
        syncRotation();

        savedInput = p.input;
        p.input = new Input(); // tomt input: spilleren står stille

        mc.setCameraEntity(cam);
    }

    @Override
    public void onDisable() {
        if (owner != null && savedInput != null) owner.input = savedInput;
        if (mc.player != null && mc.getCameraEntity() == cam) mc.setCameraEntity(mc.player);
        cam = null;
        owner = null;
        savedInput = null;
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p == null || cam == null) return;
        if (p != owner) { // ny verden/dimension/respawn: afbryd pænt
            setEnabled(false);
            return;
        }
        if (mc.getCameraEntity() != cam) mc.setCameraEntity(cam);

        // til interpolation mellem ticks
        cam.prevX = cam.getX();
        cam.prevY = cam.getY();
        cam.prevZ = cam.getZ();
        cam.lastRenderX = cam.getX();
        cam.lastRenderY = cam.getY();
        cam.lastRenderZ = cam.getZ();

        syncRotation();
        if (mc.currentScreen != null) return;

        GameOptions o = mc.options;
        double f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        double s = (o.rightKey.isPressed() ? 1 : 0) - (o.leftKey.isPressed() ? 1 : 0);
        double up = (o.jumpKey.isPressed() ? 1 : 0) - (o.sneakKey.isPressed() ? 1 : 0);

        double yaw = Math.toRadians(p.getYaw());
        double pitch = followPitch.get() ? Math.toRadians(p.getPitch()) : 0;

        // fremad (evt. med pitch) + sidelæns (vandret)
        double vx = -Math.sin(yaw) * Math.cos(pitch) * f - Math.cos(yaw) * s;
        double vy = -Math.sin(pitch) * f + up;
        double vz = Math.cos(yaw) * Math.cos(pitch) * f - Math.sin(yaw) * s;

        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (len > 0) {
            double k = speed.get() / len;
            cam.setPosition(cam.getX() + vx * k, cam.getY() + vy * k, cam.getZ() + vz * k);
        }
    }

    /** Hver frame (når HUD'en tegnes): hold kameraets blik i takt med musen, så det ikke hakker ved 20 Hz. */
    @Override
    public void renderHud(DrawContext ctx, float pt) {
        if (cam == null || owner == null || mc.player != owner) return;
        syncRotation();
    }

    private void syncRotation() {
        cam.setYaw(owner.getYaw());
        cam.setPitch(owner.getPitch());
        cam.prevYaw = cam.getYaw();
        cam.prevPitch = cam.getPitch();
    }

    private void place(double x, double y, double z) {
        cam.setPosition(x, y, z);
        cam.prevX = x;
        cam.prevY = y;
        cam.prevZ = z;
        cam.lastRenderX = x;
        cam.lastRenderY = y;
        cam.lastRenderZ = z;
    }

    /** Sluger angreb/brug/pick-klik, så intet sendes til serveren fra kameraets position. */
    private void swallowClicks() {
        if (!enabled || cam == null) return;
        GameOptions o = mc.options;
        while (o.attackKey.wasPressed()) { /* sluk */ }
        while (o.useKey.wasPressed()) { /* sluk */ }
        while (o.pickItemKey.wasPressed()) { /* sluk */ }
        o.attackKey.setPressed(false);
        o.useKey.setPressed(false);
    }
}

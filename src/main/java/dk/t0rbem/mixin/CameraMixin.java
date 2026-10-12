package dk.t0rbem.mixin;

import dk.t0rbem.modules.FreecamModule;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yRot, float xRot);
    @Shadow private boolean detached;

    @Inject(method = "setup", at = @At("TAIL"))
    private void t0rbem$freecam(CallbackInfo ci) {
        if (!FreecamModule.isActive()) return;
        FreecamModule f = FreecamModule.INSTANCE;
        float pt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        setPosition(f.lerpX(pt), f.lerpY(pt), f.lerpZ(pt));
        setRotation(f.yaw, f.pitch);
        detached = true; // så din egen spiller tegnes
    }
}

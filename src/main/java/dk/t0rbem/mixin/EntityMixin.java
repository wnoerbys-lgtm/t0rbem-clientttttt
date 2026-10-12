package dk.t0rbem.mixin;

import dk.t0rbem.modules.FreecamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** Musen kalder turn() på spilleren; med freecam drejer den kameraet i stedet. */
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void t0rbem$freecamTurn(double yRot, double xRot, CallbackInfo ci) {
        if (FreecamModule.isActive() && (Object) this == Minecraft.getInstance().player) {
            FreecamModule.INSTANCE.turn(yRot, xRot);
            ci.cancel();
        }
    }
}

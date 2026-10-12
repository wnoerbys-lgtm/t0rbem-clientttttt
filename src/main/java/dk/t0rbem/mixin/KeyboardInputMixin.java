package dk.t0rbem.mixin;

import dk.t0rbem.modules.FreecamModule;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    /** Med freecam står spilleren stille: tastetryk går kun til kameraet. */
    @Inject(method = "tick", at = @At("TAIL"))
    private void t0rbem$freecamInput(CallbackInfo ci) {
        if (FreecamModule.isActive()) {
            this.keyPresses = Input.EMPTY;
            this.moveVector = Vec2.ZERO;
        }
    }
}

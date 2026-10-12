package dk.t0rbem.modules;

import dk.t0rbem.T0rbemClient;
import dk.t0rbem.settings.NumberSetting;

public class ZoomModule extends Module {
    private final NumberSetting zoomFov = add(new NumberSetting("Zoom FOV", 30, 10, 60, 1));
    private int savedFov = -1;

    public ZoomModule() {
        super("Zoom", "Hold C nede for at zoome.", Category.RENDER, true);
    }

    @Override
    public void onTick() {
        var fov = mc.field_1690.method_41808();
        if (T0rbemClient.zoomKey.method_1434()) {
            if (savedFov == -1) savedFov = fov.method_41753();
            fov.method_41748(zoomFov.getInt());
        } else {
            restore();
        }
    }

    @Override
    public void onDisable() { restore(); }

    private void restore() {
        if (savedFov != -1) {
            mc.field_1690.method_41808().method_41748(savedFov);
            savedFov = -1;
        }
    }
}

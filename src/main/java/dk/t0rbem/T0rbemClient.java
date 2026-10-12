package dk.t0rbem;

import dk.t0rbem.modules.Module;
import dk.t0rbem.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T0rbemClient implements ClientModInitializer {
    public static final String NAME = "t0rbem client";
    public static final String ID = "t0rbem";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    public static class_304 guiKey;
    public static class_304 hudKey;
    public static class_304 zoomKey;
    public static class_304 flightKey;

    public static boolean hudVisible = true;

    @Override
    public void onInitializeClient() {
        class_304.class_11900 category = class_304.class_11900.method_74698(class_2960.method_60655(ID, "main"));

        guiKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.t0rbem.gui", class_3675.class_307.field_1668, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        hudKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.t0rbem.hud", class_3675.class_307.field_1668, GLFW.GLFW_KEY_H, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.t0rbem.zoom", class_3675.class_307.field_1668, GLFW.GLFW_KEY_C, category));

        flightKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.t0rbem.flight", class_3675.class_307.field_1668, GLFW.GLFW_KEY_G, category));

        ModuleManager.init();
        Config.load();
        ChunkTracker.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Config.save();
            ChunkTracker.save();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ChunkTracker.save());
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                class_2960.method_60655(ID, "hud"),
                T0rbemHud::render);

        LOGGER.info("{} indlæst", NAME);
    }

    private void onTick(class_310 mc) {
        while (hudKey.method_1436()) hudVisible = !hudVisible;
        while (flightKey.method_1436()) {
            Module flight = ModuleManager.get("Flight");
            if (flight != null) flight.toggle();
        }
        while (guiKey.method_1436()) {
            if (mc.field_1755 == null) mc.method_1507(new ClickGuiScreen());
        }
        if (mc.field_1724 != null) ModuleManager.tick();
    }
}

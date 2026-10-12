package dk.t0rbem.modules;

import dk.t0rbem.ChunkTracker;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.Map;
import net.minecraft.class_332;

/**
 * Chunk-kort omkring dig. En chunk bliver rød når der har været spillere i den i
 * mere end X minutter (sus) og grøn efter mere end Y timer (prime).
 */
public class ChunkMapModule extends HudModule {
    private static final int FOOTER = 11;

    private final NumberSetting radius = add(new NumberSetting("Radius", 6, 3, 12, 1));
    private final NumberSetting cell = add(new NumberSetting("Cell size", 6, 3, 12, 1));
    private final NumberSetting susMinutes = add(new NumberSetting("Sus minutes", 20, 1, 120, 1));
    private final NumberSetting primeHours = add(new NumberSetting("Prime hours", 5, 1, 24, 1));
    private final BoolSetting includeSelf = add(new BoolSetting("Include yourself", false));

    public ChunkMapModule() {
        super("Chunk Map", "Sus chunks (rød) og prime chunks (grøn) ud fra tid med spillere i chunken.", true, 9999, 200);
    }

    @Override
    public void onTick() {
        ChunkTracker.tick(mc, includeSelf.get());
    }

    private int size() { return (radius.getInt() * 2 + 1) * cell.getInt(); }

    @Override
    public int width() { return mc.field_1724 == null ? 0 : size(); }

    @Override
    public int height() { return mc.field_1724 == null ? 0 : size() + FOOTER; }

    @Override
    protected void draw(class_332 ctx, int x, int y) {
        if (mc.field_1724 == null || mc.field_1687 == null) return;

        int r = radius.getInt(), c = cell.getInt();
        int pcx = ((int) Math.floor(mc.field_1724.method_23317())) >> 4;
        int pcz = ((int) Math.floor(mc.field_1724.method_23321())) >> 4;
        Map<Long, Integer> map = ChunkTracker.current(mc);

        int susTicks = susMinutes.getInt() * 1200;
        int primeTicks = primeHours.getInt() * 72000;

        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int ticks = ChunkTracker.get(map, pcx + dx, pcz + dz);
                int color;
                if (ticks >= primeTicks) color = 0xDD22CC44;      // prime = grøn
                else if (ticks >= susTicks) color = 0xDDDD2222;   // sus = rød
                else if (ticks > 0) color = 0x66AAAAAA;           // set, men ikke længe nok endnu
                else color = 0x33FFFFFF;
                int cx = x + (dx + r) * c, cy = y + (dz + r) * c;
                ctx.method_25294(cx, cy, cx + c - 1, cy + c - 1, color);
            }
        }

        // Din egen chunk markeres med hvid ramme
        int mx = x + r * c, my = y + r * c;
        ctx.method_25294(mx - 1, my - 1, mx + c, my, 0xFFFFFFFF);
        ctx.method_25294(mx - 1, my + c - 1, mx + c, my + c, 0xFFFFFFFF);
        ctx.method_25294(mx - 1, my, mx, my + c - 1, 0xFFFFFFFF);
        ctx.method_25294(mx + c - 1, my, mx + c, my + c - 1, 0xFFFFFFFF);

        int here = ChunkTracker.get(map, pcx, pcz) / 1200;
        String label = "Chunk " + pcx + "," + pcz + ": " + here + " min";
        ctx.method_25303(mc.field_1772, label, x, y + size() + 2, textColor());
    }
}

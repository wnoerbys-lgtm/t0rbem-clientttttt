package dk.t0rbem.modules;

import dk.t0rbem.ChunkTracker;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Prime Chunk Finder: markerer chunks hvor der er set spillere i mange timer i alt
 * (samme data og definition som den grønne farve i Chunk Map).
 */
public class PrimeChunkModule extends ChunkFinderModule {
    private final NumberSetting primeHours = add(new NumberSetting("Prime hours", 5, 1, 24, 1));
    private final BoolSetting includeSelf = add(new BoolSetting("Include yourself", false));

    public PrimeChunkModule() {
        super("Prime Chunk Finder", "Markerer chunks hvor spillere har opholdt sig i mange timer (typisk en hovedbase).");
    }

    @Override protected int color() { return 0xFF55FF55; }
    @Override protected String title() { return "Prime"; }

    @Override
    protected void perTick() {
        ChunkTracker.tick(mc, includeSelf.get());
    }

    @Override
    protected void collect(List<Target> out) {
        Map<Long, Integer> time = ChunkTracker.current(mc);
        int minTicks = primeHours.getInt() * 72000;
        for (var e : time.entrySet()) {
            if (e.getValue() < minTicks) continue;
            long k = e.getKey();
            out.add(new Target(ChunkTracker.unpackX(k), ChunkTracker.unpackZ(k), 0, 0, true,
                    String.format(Locale.ROOT, "%.1f h", e.getValue() / 72000.0)));
        }
    }
}

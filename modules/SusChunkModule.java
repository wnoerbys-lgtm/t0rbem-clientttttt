package dk.t0rbem.modules;

import dk.t0rbem.ChunkScanner;
import dk.t0rbem.ChunkTracker;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sus Chunk Finder: markerer chunks der ligner at der er aktivitet/base – enten mange
 * opbevaringsblokke (storage-scan) eller spillere der er set i chunken længe (spillertid).
 */
public class SusChunkModule extends ChunkFinderModule {
    private final BoolSetting useStorage = add(new BoolSetting("Storage scan", true));
    private final BoolSetting useTime = add(new BoolSetting("Player time", true));
    private final NumberSetting minScore = add(new NumberSetting("Min score", 6, 1, 60, 1));
    private final NumberSetting susMinutes = add(new NumberSetting("Sus minutes", 20, 1, 120, 1));
    private final NumberSetting scanRadius = add(new NumberSetting("Scan radius", 24, 8, 32, 1));
    private final BoolSetting includeSelf = add(new BoolSetting("Include yourself", false));

    public SusChunkModule() {
        super("Sus Chunk Finder",
                "Markerer mistænkelige chunks: mange kister/hoppers/ender chests eller spillere set længe i chunken.");
    }

    @Override protected int color() { return 0xFFFF5555; }
    @Override protected String title() { return "Sus"; }

    @Override
    protected void perTick() {
        if (useTime.get()) ChunkTracker.tick(mc, includeSelf.get());
    }

    @Override
    protected void collect(List<Target> out) {
        Map<Long, ChunkScanner.Hit> storage = Map.of();
        if (useStorage.get()) {
            ChunkScanner.scan(mc, scanRadius.getInt());
            storage = ChunkScanner.current(mc);
        }
        Map<Long, Integer> time = useTime.get() ? ChunkTracker.current(mc) : Map.of();
        int minTicks = susMinutes.getInt() * 1200;
        int min = minScore.getInt();

        Set<Long> keys = new HashSet<>();
        for (var e : storage.entrySet()) if (e.getValue().score() >= min) keys.add(e.getKey());
        for (var e : time.entrySet()) if (e.getValue() >= minTicks) keys.add(e.getKey());

        for (long k : keys) {
            ChunkScanner.Hit h = storage.get(k);
            int ticks = time.getOrDefault(k, 0);

            StringBuilder d = new StringBuilder();
            if (h != null) d.append(h.storage()).append(" storage, score ").append(h.score());
            if (ticks >= minTicks) {
                if (d.length() > 0) d.append("  |  ");
                d.append(ticks / 1200).append(" min");
            }
            out.add(new Target(ChunkTracker.unpackX(k), ChunkTracker.unpackZ(k),
                    h != null ? h.minY() : 0, h != null ? h.maxY() : 0, h == null, d.toString()));
        }
    }
}

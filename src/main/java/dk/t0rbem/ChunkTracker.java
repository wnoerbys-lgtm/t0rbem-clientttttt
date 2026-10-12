package dk.t0rbem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_642;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tæller hvor mange ticks der har været spillere i hver chunk.
 * Gemmes pr. server og dimension i config/t0rbem-chunks.json.
 */
public class ChunkTracker {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Map<Long, Integer>> DATA = new HashMap<>();
    private static boolean dirty;
    private static int sinceSave;

    public static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int unpackX(long k) { return (int) (k >> 32); }
    private static int unpackZ(long k) { return (int) k; }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("t0rbem-chunks.json");
    }

    public static String worldKey(class_310 mc) {
        class_642 info = mc.method_1558();
        String server = info != null ? info.field_3761 : "singleplayer";
        String dim = mc.field_1687 != null ? mc.field_1687.method_27983().method_29177().toString() : "unknown";
        return server + "|" + dim;
    }

    public static Map<Long, Integer> current(class_310 mc) {
        return DATA.computeIfAbsent(worldKey(mc), k -> new HashMap<>());
    }

    public static int get(Map<Long, Integer> map, int chunkX, int chunkZ) {
        return map.getOrDefault(pack(chunkX, chunkZ), 0);
    }

    public static void tick(class_310 mc, boolean includeSelf) {
        if (mc.field_1687 == null) return;
        Map<Long, Integer> map = current(mc);
        Set<Long> seen = new HashSet<>();
        for (var player : mc.field_1687.method_18456()) {
            if (player == mc.field_1724 && !includeSelf) continue;
            int cx = ((int) Math.floor(player.method_23317())) >> 4;
            int cz = ((int) Math.floor(player.method_23321())) >> 4;
            seen.add(pack(cx, cz));
        }
        for (long k : seen) map.merge(k, 1, Integer::sum);
        if (!seen.isEmpty()) dirty = true;

        if (++sinceSave >= 1200) { // ca. hvert minut
            sinceSave = 0;
            if (dirty) save();
        }
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (String world : root.keySet()) {
                Map<Long, Integer> m = new HashMap<>();
                JsonObject w = root.getAsJsonObject(world);
                for (String key : w.keySet()) {
                    String[] p = key.split(",");
                    m.put(pack(Integer.parseInt(p[0]), Integer.parseInt(p[1])), w.get(key).getAsInt());
                }
                DATA.put(world, m);
            }
        } catch (Exception e) {
            T0rbemClient.LOGGER.warn("Kunne ikke læse chunk-data", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (var world : DATA.entrySet()) {
                JsonObject w = new JsonObject();
                for (var e : world.getValue().entrySet()) {
                    w.addProperty(unpackX(e.getKey()) + "," + unpackZ(e.getKey()), e.getValue());
                }
                root.add(world.getKey(), w);
            }
            Files.writeString(file(), GSON.toJson(root));
            dirty = false;
        } catch (Exception e) {
            T0rbemClient.LOGGER.warn("Kunne ikke gemme chunk-data", e);
        }
    }
}

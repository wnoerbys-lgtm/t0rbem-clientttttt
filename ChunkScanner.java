package dk.t0rbem;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.entity.BarrelBlockEntity;

/**
 * Scanner de chunks din klient har indlæst efter opbevaring (kister, tønder, hoppers, ender chests,
 * shulkers m.m.) og giver hver chunk en score. Rent klientside – læser kun data serveren allerede har sendt.
 * Resultater huskes pr. server+dimension, også efter chunken er blevet unloadet (kun i denne session).
 */
public final class ChunkScanner {
    /** score = vægtet sum, storage = antal blokke, minY/maxY = højdespænd for dem. */
    public record Hit(int score, int storage, int minY, int maxY) {}

    private static final Map<String, Map<Long, Hit>> DATA = new HashMap<>();

    private ChunkScanner() {}

    public static Map<Long, Hit> current(MinecraftClient mc) {
        return DATA.computeIfAbsent(ChunkTracker.worldKey(mc), k -> new HashMap<>());
    }

    /** Scanner alle indlæste chunks inden for radius (i chunks) omkring spilleren. */
    public static void scan(MinecraftClient mc, int radiusChunks) {
        if (mc.world == null || mc.player == null) return;
        Map<Long, Hit> map = current(mc);
        int pcx = ((int) Math.floor(mc.player.getX())) >> 4;
        int pcz = ((int) Math.floor(mc.player.getZ())) >> 4;

        for (int cx = pcx - radiusChunks; cx <= pcx + radiusChunks; cx++) {
            for (int cz = pcz - radiusChunks; cz <= pcz + radiusChunks; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue; // ikke indlæst: behold gammelt resultat
                Hit h = analyse(chunk);
                long key = ChunkTracker.pack(cx, cz);
                if (h == null) map.remove(key);
                else map.put(key, h);
            }
        }
    }

    private static Hit analyse(WorldChunk chunk) {
        int score = 0, storage = 0, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            int w = weight(be);
            if (w == 0) continue;
            score += w;
            storage++;
            int y = be.getPos().getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return storage == 0 ? null : new Hit(score, storage, minY, maxY);
    }

    /** Ting der aldrig/sjældent genereres naturligt vægter højest. */
    private static int weight(BlockEntity be) {
        if (be instanceof EnderChestBlockEntity) return 4;   // ender chest
        if (be instanceof ShulkerBoxBlockEntity) return 4;   // shulker box
        if (be instanceof HopperBlockEntity) return 3;   // hopper
        if (be instanceof ChestBlockEntity                // chest / trapped chest
                || be instanceof BarrelBlockEntity         // barrel
                || be instanceof AbstractFurnaceBlockEntity         // furnace m.fl.
                || be instanceof DispenserBlockEntity) return 1; // dispenser / dropper
        return 0;
    }
}

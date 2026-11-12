package xyz.iwolfking.woldsvaults.medallions.assassins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * The live set of spawned greed assassins, keyed by level and holding UUIDs only; entries are dropped on death and
 * pruned lazily during {@link #forEach}.
 */
public final class GreedAssassinRegistry {
    private static final Map<ResourceKey<Level>, Set<UUID>> TRACKED = new ConcurrentHashMap<>();

    private GreedAssassinRegistry() {
    }

    public static void track(ServerLevel level, UUID assassinId) {
        if (level == null || assassinId == null) {
            return;
        }
        TRACKED.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(assassinId);
    }

    public static void forget(Entity assassin) {
        if (assassin == null) {
            return;
        }
        Set<UUID> tracked = TRACKED.get(assassin.level.dimension());
        if (tracked != null) {
            tracked.remove(assassin.getUUID());
        }
    }

    /** Visits every tracked assassin that still resolves, pruning the ones that do not. */
    public static void forEach(MinecraftServer server, BiConsumer<ServerLevel, LivingEntity> action) {
        if (server == null) {
            return;
        }
        Iterator<Map.Entry<ResourceKey<Level>, Set<UUID>>> levels = TRACKED.entrySet().iterator();
        while (levels.hasNext()) {
            Map.Entry<ResourceKey<Level>, Set<UUID>> entry = levels.next();
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) {
                levels.remove();
                continue;
            }
            Iterator<UUID> assassins = entry.getValue().iterator();
            while (assassins.hasNext()) {
                Entity entity = level.getEntity(assassins.next());
                if (!(entity instanceof LivingEntity assassin) || !assassin.isAlive()) {
                    assassins.remove();
                    continue;
                }
                action.accept(level, assassin);
            }
            if (entry.getValue().isEmpty()) {
                levels.remove();
            }
        }
    }

    public static void clearAll() {
        TRACKED.clear();
    }
}

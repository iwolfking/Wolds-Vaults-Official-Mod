package xyz.iwolfking.woldsvaults.api.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map tier (0-5) per running vault, populated when a tier-imprinted crystal configures its
 * vault and read at strongbox loot generation. Vaults without a tier (regular crystals, or a
 * server restart mid-vault) simply have no entry and report -1.
 */
public class VaultMapTierCache {
    private static final Map<UUID, Integer> TIER_CACHE = new ConcurrentHashMap<>();

    public static void put(UUID vaultId, int tier) {
        TIER_CACHE.put(vaultId, tier);
    }

    public static int get(UUID vaultId) {
        return TIER_CACHE.getOrDefault(vaultId, -1);
    }

    public static void invalidate(UUID vaultId) {
        TIER_CACHE.remove(vaultId);
    }
}

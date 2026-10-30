package xyz.iwolfking.woldsvaults.api.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoyaleVaultCache {

    private static final Map<UUID, Boolean> IS_ROYALE_CACHE = new ConcurrentHashMap<>();

    public static boolean isRoyale(UUID vaultId, java.util.function.Supplier<Boolean> computer) {
        return IS_ROYALE_CACHE.computeIfAbsent(vaultId, id -> computer.get());
    }

    public static void invalidate(UUID vaultId) {
        IS_ROYALE_CACHE.remove(vaultId);
    }
}
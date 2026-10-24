package xyz.iwolfking.woldsvaults.integration.bettercombat;

import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public final class DualWieldSnapshotManager {
    private static final Map<UUID, AttributeSnapshot> CACHE = new HashMap<>();

    private record ActiveSnapshot(UUID playerId, AttributeSnapshot snapshot) {}

    private static final ThreadLocal<Deque<ActiveSnapshot>> ACTIVE =
            ThreadLocal.withInitial(ArrayDeque::new);

    private DualWieldSnapshotManager() {}

    public static AttributeSnapshot getSelected(ServerPlayer player) {
        Deque<ActiveSnapshot> stack = ACTIVE.get();

        for (ActiveSnapshot active : stack) {
            if (active.playerId().equals(player.getUUID())) {
                return active.snapshot();
            }
        }

        return null;
    }

    public static void withOffhandSnapshot(
            ServerPlayer player,
            Runnable action
    ) {
        AttributeSnapshot snapshot = CACHE.computeIfAbsent(
                player.getUUID(),
                id -> calculate(player)
        );

        Deque<ActiveSnapshot> stack = ACTIVE.get();
        stack.push(new ActiveSnapshot(player.getUUID(), snapshot));

        try {
            action.run();
        } finally {
            stack.pop();

            if (stack.isEmpty()) {
                ACTIVE.remove();
            }
        }
    }

    private static AttributeSnapshot calculate(ServerPlayer player) {
        AttributeSnapshot snapshot = new AttributeSnapshot();

        DualWieldSnapshotComputer.computeForWeapon(
                player,
                player.getOffhandItem(),
                snapshot
        );

        return snapshot;
    }

    public static void invalidate(ServerPlayer player) {
        CACHE.remove(player.getUUID());
    }
}
package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Minor-transfer slots: each god's slots carry that god's own learned minor stars to whichever god is
 * active, and go dormant while that god is the active one. Assignments go through {@link #assign}.
 */
public final class MinorTransferSlots {
    public enum Result {
        OK,
        UNCHANGED,
        NO_TREE,
        SLOT_LOCKED,
        NOT_A_MINOR,
        WRONG_GOD,
        DISABLED,
        NOT_LEARNED
    }

    private MinorTransferSlots() {
    }

    /**
     * The effect ids a god's transfer slots carry: only the first {@code unlockedSlots} count, holes are
     * skipped, and an entry no longer transferable is skipped and reported to {@code onStale}.
     */
    public static List<String> liveTransfers(VaultGod god, GodAlignmentData.GodState state, int unlockedSlots,
                                             Consumer<String> onStale) {
        int limit = Math.min(unlockedSlots, state.minorTransfers.size());
        List<String> live = new ArrayList<>();
        for (int slot = 0; slot < limit; slot++) {
            String id = state.minorTransfers.get(slot);
            if (id.isEmpty()) {
                continue;
            }
            if (isTransferable(god, id, state.spentPoints)) {
                live.add(id);
            } else {
                onStale.accept(id);
            }
        }
        return live;
    }

    /**
     * Whether {@code god}'s slots may carry {@code effectId} for a player whose ledger is {@code ledger}:
     * a minor of this very god, placed and enabled in its tree, that the player has bought.
     */
    public static boolean isTransferable(VaultGod god, @Nullable String effectId, Map<String, Integer> ledger) {
        if (effectId == null || effectId.isEmpty()) {
            return false;
        }
        GodEffect effect = GodNodeRegistry.effect(effectId).orElse(null);
        if (effect == null || effect.god() != god || GodNodeRegistry.effectType(effectId) != GodNodeType.MINOR) {
            return false;
        }
        GodNode placement = GodNodeRegistry.tree(god).map(tree -> tree.placementOf(effectId)).orElse(null);
        if (placement == null || !placement.enabled()) {
            return false;
        }
        return ledger.getOrDefault(effectId, 0) > 0;
    }

    /**
     * Puts {@code effectId} into {@code slot} of {@code god}'s transfer slots, or clears it for a null or
     * empty id. On success the gate cache, attribute snapshot and tick contributors are all refreshed.
     */
    public static Result assign(ServerPlayer player, VaultGod god, int slot, @Nullable String effectId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            WoldsVaults.LOGGER.warn("Transfer slot assignment for {} had no server attached; ignoring it.",
                    player.getGameProfile().getName());
            return Result.UNCHANGED;
        }
        GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
        if (tree == null) {
            return Result.NO_TREE;
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        if (slot < 0 || slot >= data.getMinorTransferSlots(player.getUUID(), god)) {
            return Result.SLOT_LOCKED;
        }
        String id = effectId == null ? "" : effectId;
        if (!id.isEmpty()) {
            GodEffect effect = GodNodeRegistry.effect(id).orElse(null);
            if (effect == null || GodNodeRegistry.effectType(id) != GodNodeType.MINOR) {
                return Result.NOT_A_MINOR;
            }
            if (effect.god() != god) {
                return Result.WRONG_GOD;
            }
            GodNode placement = tree.placementOf(id);
            if (placement == null || !placement.enabled()) {
                return Result.DISABLED;
            }
            if (data.getPointsIn(player.getUUID(), god, id) <= 0) {
                return Result.NOT_LEARNED;
            }
        }
        if (!data.setMinorTransfer(player, god, slot, id)) {
            return Result.UNCHANGED;
        }
        GodNodeCache.refresh(player);
        AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(player);
        GodNodeTicker.reconcile(player);
        return Result.OK;
    }
}

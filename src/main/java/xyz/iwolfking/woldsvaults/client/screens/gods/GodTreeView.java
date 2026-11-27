package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Everything the gods screen reads about a player's standing with the gods. {@link LiveGodTreeView} mirrors the
 * viewer's own synced data; other implementations show a fixed standing, such as a saved armory build.
 */
public interface GodTreeView {

    /** Whether this view mirrors the viewer's own live standing: only then are previews requested and edits offered. */
    boolean isLive();

    /** Increments whenever the data behind this view changes. */
    long revision();

    long getXp(VaultGod god);

    int getLevel(VaultGod god);

    int getSacrifices(VaultGod god);

    int getUnspentPoints(VaultGod god);

    int getPiety(VaultGod god);

    int getReputation(VaultGod god);

    Map<String, Integer> getSpentLedger(VaultGod god);

    Set<String> getPurchasedTreeNodes(VaultGod god);

    boolean isTreeNodePurchased(VaultGod god, String nodeId);

    /** The raw content of one transfer slot, empty for a hole or a slot never written. */
    Optional<String> getMinorTransferSlot(VaultGod god, int slot);

    /** The slot index holding {@code effectId} in {@code god}'s transfer slots, or -1. */
    int findMinorTransferSlot(VaultGod god, String effectId);

    /** The god whose charm is equipped, if any. */
    Optional<VaultGod> charmGod();

    default int getMinorTransferSlots(VaultGod god) {
        return GodLevels.minorTransferSlots(this.getLevel(god));
    }

    /** Whether a slot is unlocked and holds a learned minor star of that god. */
    default boolean isMinorTransferLive(VaultGod god, int slot) {
        if (slot < 0 || slot >= this.getMinorTransferSlots(god)) {
            return false;
        }
        return this.getMinorTransferSlot(god, slot)
                .map(id -> MinorTransferSlots.isTransferable(god, id, this.getSpentLedger(god)))
                .orElse(false);
    }

    default boolean isCharmActive(VaultGod god) {
        return this.charmGod().filter(active -> active == god).isPresent();
    }

    /** Whether {@code god}'s constellation is shown; it stays uncharted below the charting reputation. */
    default boolean isCharted(VaultGod god) {
        return this.getReputation(god) >= GodCharting.reputationRequired();
    }
}

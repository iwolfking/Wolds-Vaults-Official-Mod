package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client-side mirror of the local player's {@link GodAlignmentData} state, replaced wholesale by
 * every sync packet. Read-only for consumers; the server remains authoritative. {@link #revision()}
 * increments on every accepted sync so screens can poll for changes without holding references.
 */
public final class ClientGodAlignmentData {
    private static EnumMap<VaultGod, GodAlignmentData.GodState> states = new EnumMap<>(VaultGod.class);
    private static EnumMap<VaultGod, Integer> piety = new EnumMap<>(VaultGod.class);
    private static long revision = 0L;

    private ClientGodAlignmentData() {
    }

    public static void accept(Map<VaultGod, GodAlignmentData.GodState> synced, Map<VaultGod, Integer> syncedPiety) {
        EnumMap<VaultGod, GodAlignmentData.GodState> replacement = new EnumMap<>(VaultGod.class);
        replacement.putAll(synced);
        states = replacement;
        EnumMap<VaultGod, Integer> pietyReplacement = new EnumMap<>(VaultGod.class);
        pietyReplacement.putAll(syncedPiety);
        piety = pietyReplacement;
        revision++;
    }

    public static void clear() {
        states = new EnumMap<>(VaultGod.class);
        piety = new EnumMap<>(VaultGod.class);
        revision++;
    }

    public static int getPiety(VaultGod god) {
        return piety.getOrDefault(god, 0);
    }

    public static long revision() {
        return revision;
    }

    public static long getXp(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? 0L : state.xp;
    }

    public static int getLevel(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? 0 : GodLevels.gatedLevel(state.xp, state.sacrifices);
    }

    public static int getSacrifices(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? 0 : state.sacrifices;
    }

    public static int getSpentPoints(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        if (state == null) {
            return 0;
        }
        int spent = 0;
        for (int points : state.spentPoints.values()) {
            spent += points;
        }
        return spent;
    }

    public static int getUnspentPoints(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        int bonus = state == null ? 0 : state.bonusPoints;
        return GodLevels.totalPointsForLevel(getLevel(god)) + bonus - getSpentPoints(god);
    }

    public static Map<String, Integer> getSpentLedger(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? Collections.emptyMap() : Collections.unmodifiableMap(state.spentPoints);
    }

    public static List<String> getMinorTransfers(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? Collections.emptyList() : Collections.unmodifiableList(state.minorTransfers);
    }

    public static int getMinorTransferSlots(VaultGod god) {
        return GodLevels.minorTransferSlots(getLevel(god));
    }

    public static int getAltarCompletions(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? 0 : state.altarCompletions;
    }

    public static Set<String> getPurchasedTreeNodes(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? Collections.emptySet() : Collections.unmodifiableSet(state.treeNodes);
    }

    public static boolean isTreeNodePurchased(VaultGod god, String nodeId) {
        GodAlignmentData.GodState state = states.get(god);
        return state != null && state.treeNodes.contains(nodeId);
    }
}

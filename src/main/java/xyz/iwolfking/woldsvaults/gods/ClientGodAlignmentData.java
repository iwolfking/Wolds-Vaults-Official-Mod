package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side mirror of the local player's {@link GodAlignmentData} state, replaced wholesale by
 * every sync packet. Read-only for consumers; the server remains authoritative.
 */
public final class ClientGodAlignmentData {
    private static EnumMap<VaultGod, GodAlignmentData.GodState> states = new EnumMap<>(VaultGod.class);

    private ClientGodAlignmentData() {
    }

    public static void accept(Map<VaultGod, GodAlignmentData.GodState> synced) {
        EnumMap<VaultGod, GodAlignmentData.GodState> replacement = new EnumMap<>(VaultGod.class);
        replacement.putAll(synced);
        states = replacement;
    }

    public static void clear() {
        states = new EnumMap<>(VaultGod.class);
    }

    public static long getXp(VaultGod god) {
        GodAlignmentData.GodState state = states.get(god);
        return state == null ? 0L : state.xp;
    }

    public static int getLevel(VaultGod god) {
        return GodLevels.levelForXp(getXp(god));
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
}

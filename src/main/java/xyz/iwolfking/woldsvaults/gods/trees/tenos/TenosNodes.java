package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Node identity for the Tenos (The Omniscient) god tree, sheet rows r92-r118.
 *
 * <p>Every Tenos stat row is a two-point node, so the values here are per point: Hoarder at two
 * points is +50% item quantity, matching the sheet's "25%+, 50%+" columns.
 */
public final class TenosNodes {
    public static final VaultGod GOD = VaultGod.TENOS;

    public static final String HOARDER = "tenos_hoarder";
    public static final String TREASURER = "tenos_treasurer";
    public static final String MAGICAL = "tenos_magical";
    public static final String RESERVES = "tenos_reserves";
    public static final String CAREFUL = "tenos_careful";
    public static final String WIDE_INFLUENCE = "tenos_wide_influence";
    public static final String OMEGA_VAULT = "tenos_omega_vault";
    public static final String LOOTING_ENGINE = "tenos_looting_engine";
    public static final String MANA_STARVED = "tenos_mana_starved";
    public static final String DEEP_RESERVES = "tenos_deep_reserves";
    public static final String BARTER_EXPERT = "tenos_barter_expert";
    public static final String NOSE_FOR_TREASURE = "tenos_nose_for_treasure";
    public static final String DOMAIN_EXPANSION = "tenos_domain_expansion";
    public static final String MASSIVE_CHESTS = "tenos_massive_chests";
    public static final String INDIANA_JONES = "tenos_indiana_jones";
    public static final String EXPERT_LOOTER = "tenos_expert_looter";
    public static final String MASTER_OF_CHESTS = "tenos_master_of_chests";
    public static final String GLOBAL_VEINS = "tenos_global_veins";
    public static final String ADVANCED_EXTRACTION = "tenos_advanced_extraction";
    public static final String DRILLMASTER = "tenos_drillmaster";
    public static final String SACKED = "tenos_sacked";
    public static final String SACK_OF_MOBS = "tenos_sack_of_mobs";
    public static final String UNSTOPPABLE_GREED = "tenos_unstoppable_greed";
    public static final String WEALTHY_PATRON = "tenos_wealthy_patron";
    public static final String GOLD_PLATING = "tenos_gold_plating";
    public static final String CASH_HUNTER = "tenos_cash_hunter";
    public static final String CHALLENGE_TACKLER = "tenos_challenge_tackler";

    /** Plain stat rows, per point: the subset eligible for foreign-tree carryover. */
    public static final Map<String, StatEntry> BASIC_STATS = basicStats();

    /** Every minor node of this tree, i.e. everything transferable into a minor-transfer slot. */
    public static final Set<String> MINORS = Set.of(
            OMEGA_VAULT, MANA_STARVED, DEEP_RESERVES, BARTER_EXPERT, NOSE_FOR_TREASURE,
            DOMAIN_EXPANSION, INDIANA_JONES, EXPERT_LOOTER, MASTER_OF_CHESTS, GLOBAL_VEINS,
            DRILLMASTER, SACKED, SACK_OF_MOBS, UNSTOPPABLE_GREED, WEALTHY_PATRON, GOLD_PLATING,
            CASH_HUNTER, CHALLENGE_TACKLER);

    /** Nodes registered with no behaviour, and why. Kept so ids stay reserved and reportable. */
    public static final Map<String, String> INERT = Map.of(
            MASTER_OF_CHESTS, "\"Omega cascading\" is not a stat: cascade runs at worldgen before any player exists, "
                    + "so the effect would be decided by whoever starts the vault. Needs a design ruling (doc 3.11, 8.5).");

    private TenosNodes() {
    }

    private static Map<String, StatEntry> basicStats() {
        Map<String, StatEntry> stats = new LinkedHashMap<>();
        stats.put(HOARDER, new StatEntry(ModGearAttributes.ITEM_QUANTITY, 0.25F));
        stats.put(TREASURER, new StatEntry(ModGearAttributes.ITEM_RARITY, 0.25F));
        stats.put(MAGICAL, new StatEntry(ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, 0.40F));
        stats.put(RESERVES, new StatEntry(ModGearAttributes.MANA_ADDITIVE_PERCENTILE, 0.10F));
        stats.put(CAREFUL, new StatEntry(ModGearAttributes.TRAP_DISARMING, 0.15F));
        stats.put(WIDE_INFLUENCE, new StatEntry(ModGearAttributes.AREA_OF_EFFECT, 0.10F));
        stats.put(ADVANCED_EXTRACTION, new StatEntry(ModGearAttributes.COPIOUSLY, 0.20F));
        return Map.copyOf(stats);
    }

    public static boolean owns(String nodeId) {
        return nodeId != null && nodeId.startsWith("tenos_");
    }

    /** Points a player has in a Tenos minor, honouring minor-transfer selection. */
    public static int minorPoints(ServerPlayer player, String nodeId) {
        return GodNodeGate.minorPoints(player, GOD, nodeId);
    }

    public static boolean hasMinor(ServerPlayer player, String nodeId) {
        return minorPoints(player, nodeId) > 0;
    }

    public static boolean hasMajor(ServerPlayer player, String nodeId) {
        return GodNodeGate.isActiveMajor(player, GOD, nodeId);
    }

    /** A stat row: one gear attribute and the value one spent point contributes. */
    public record StatEntry(VaultGearAttribute<Float> attribute, float perPoint) {
    }
}

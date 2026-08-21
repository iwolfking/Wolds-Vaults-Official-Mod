package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Node identity for the Tenos (The Omniscient) god tree, sheet rows r92-r118.
 *
 * <p>A stat the sheet lists as a pair ({@code 25%+, 50%+}) is two ids, not one node with two
 * ranks: the shallow placements in the tree are the base id at the first value and the deep ones
 * are the {@code _ii} id at the second, each paying its own value per star with no ceiling. Both
 * bands are plain {@link StatEntry} rows, so the provider needs to know nothing about banding.
 * Which placement is which is decided by depth in
 * {@code tree-drafts/export_tenos_wiring.py}, not here.
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
    public static final String PIOUS_DEVOTION = "tenos_pious_devotion";

    public static final String HOARDER_II = "tenos_hoarder_ii";
    public static final String TREASURER_II = "tenos_treasurer_ii";
    public static final String MAGICAL_II = "tenos_magical_ii";
    public static final String RESERVES_II = "tenos_reserves_ii";
    public static final String CAREFUL_II = "tenos_careful_ii";
    public static final String WIDE_INFLUENCE_II = "tenos_wide_influence_ii";
    public static final String ADVANCED_EXTRACTION_II = "tenos_advanced_extraction_ii";

    /** Plain stat rows, per point: the subset eligible for foreign-tree carryover. */
    public static final Map<String, StatEntry> BASIC_STATS = basicStats();

    /** Every minor node of this tree, i.e. everything transferable into a minor-transfer slot. */
    public static final Set<String> MINORS = Set.of(
            OMEGA_VAULT, MANA_STARVED, DEEP_RESERVES, BARTER_EXPERT, NOSE_FOR_TREASURE,
            DOMAIN_EXPANSION, INDIANA_JONES, EXPERT_LOOTER, MASTER_OF_CHESTS, GLOBAL_VEINS,
            DRILLMASTER, SACKED, SACK_OF_MOBS, UNSTOPPABLE_GREED, WEALTHY_PATRON, GOLD_PLATING,
            CASH_HUNTER, CHALLENGE_TACKLER);

    /** Nodes registered with no behaviour, and why. Kept so ids stay reserved and reportable. */
    public static final Map<String, String> INERT = Map.of();

    private TenosNodes() {
    }

    private static Map<String, StatEntry> basicStats() {
        Map<String, StatEntry> stats = new LinkedHashMap<>();
        stats.put(HOARDER, new StatEntry(HOARDER, ModGearAttributes.ITEM_QUANTITY));
        stats.put(TREASURER, new StatEntry(TREASURER, ModGearAttributes.ITEM_RARITY));
        stats.put(MAGICAL, new StatEntry(MAGICAL, ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE));
        stats.put(RESERVES, new StatEntry(RESERVES, ModGearAttributes.MANA_ADDITIVE_PERCENTILE));
        stats.put(CAREFUL, new StatEntry(CAREFUL, ModGearAttributes.TRAP_DISARMING));
        stats.put(WIDE_INFLUENCE, new StatEntry(WIDE_INFLUENCE, ModGearAttributes.AREA_OF_EFFECT));
        stats.put(ADVANCED_EXTRACTION, new StatEntry(ADVANCED_EXTRACTION, ModGearAttributes.COPIOUSLY));
        stats.put(HOARDER_II, new StatEntry(HOARDER_II, ModGearAttributes.ITEM_QUANTITY));
        stats.put(TREASURER_II, new StatEntry(TREASURER_II, ModGearAttributes.ITEM_RARITY));
        stats.put(MAGICAL_II, new StatEntry(MAGICAL_II, ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE));
        stats.put(RESERVES_II, new StatEntry(RESERVES_II, ModGearAttributes.MANA_ADDITIVE_PERCENTILE));
        stats.put(CAREFUL_II, new StatEntry(CAREFUL_II, ModGearAttributes.TRAP_DISARMING));
        stats.put(WIDE_INFLUENCE_II, new StatEntry(WIDE_INFLUENCE_II, ModGearAttributes.AREA_OF_EFFECT));
        stats.put(ADVANCED_EXTRACTION_II, new StatEntry(ADVANCED_EXTRACTION_II, ModGearAttributes.COPIOUSLY));
        return Map.copyOf(stats);
    }

    public static boolean owns(String nodeId) {
        return nodeId != null && nodeId.startsWith("tenos_");
    }

    /** Raw ledger read for one node, ignoring which god is active. */
    public static int ledgerPoints(ServerPlayer player, String nodeId) {
        if (player.getServer() == null) {
            return 0;
        }
        return GodAlignmentData.get(player.getServer()).getPointsIn(player.getUUID(), GOD, nodeId);
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

    /** A stat row: one gear attribute and the configured value one spent point contributes. */
    public record StatEntry(String nodeId, VaultGearAttribute<Float> attribute) {
        public float perPoint() {
            return GodNodeValues.value(this.nodeId);
        }
    }
}

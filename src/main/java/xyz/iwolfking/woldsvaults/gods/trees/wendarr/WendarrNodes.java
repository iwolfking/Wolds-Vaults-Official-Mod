package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Node identity for the Wendarr (The Timekeeper) god tree, sheet rows r63-r88.
 *
 * <p>Ids are the single source of truth shared by the attribute provider, every functional
 * handler and the minor-transfer resolver. Plain stat rows carry their attribute and per-point
 * value here; everything else is behaviour and lives in a dedicated handler.
 */
public final class WendarrNodes {
    public static final VaultGod GOD = VaultGod.WENDARR;

    public static final String FRUIT_CONISSOUR = "wendarr_fruit_conissour";
    public static final String SPEEDY = "wendarr_speedy";
    public static final String HEAVILY_EFFECTED = "wendarr_heavily_effected";
    public static final String MASTER_IMBUER = "wendarr_master_imbuer";
    public static final String EXTRACTION_SUPERVISER = "wendarr_extraction_superviser";
    public static final String SPEEDY_CASTER = "wendarr_speedy_caster";
    public static final String PIOUS_DEVOTION = "wendarr_pious_devotion";
    public static final String EXPERT_EATER = "wendarr_expert_eater";
    public static final String PRISTINE_CONDITION = "wendarr_pristine_condition";
    public static final String EFFICIENT_STEPS = "wendarr_efficient_steps";
    public static final String LEGEND_OF_THE_PEAR = "wendarr_legend_of_the_pear";
    public static final String GLUTTON = "wendarr_glutton";
    public static final String PACED_STRIKES = "wendarr_paced_strikes";
    public static final String EDGE_OF_TIME = "wendarr_edge_of_time";
    public static final String EXTENDER = "wendarr_extender";
    public static final String TEMPORAL_BREAKING = "wendarr_temporal_breaking";
    public static final String PLUSHIE_LOVER = "wendarr_plushie_lover";
    public static final String TEMPORAL_SHIELDING = "wendarr_temporal_shielding";
    public static final String TOUGH_STOMACH = "wendarr_tough_stomach";
    public static final String CLOCK_ARTIFICIER = "wendarr_clock_artificier";
    public static final String PYLON_WHISPERER = "wendarr_pylon_whisperer";
    public static final String GARDENER = "wendarr_gardener";
    public static final String FRUITY = "wendarr_fruity";
    public static final String ARMORED_EXTRACTORS = "wendarr_armored_extractors";
    public static final String THE_DECKLESS = "wendarr_the_deckless";
    public static final String SPEED_DEMON = "wendarr_speed_demon";
    public static final String QUICK_SEARCH = "wendarr_quick_search";

    /** Plain stat rows (sheet type "Stat"): the subset eligible for foreign-tree carryover. */
    public static final Map<String, StatEntry> BASIC_STATS = basicStats();

    /** Stat-shaped minor rows: full value on the active tree and through minor transfer only. */
    public static final Map<String, StatEntry> MINOR_STATS = Map.of(
            FRUITY, new StatEntry(ModGearAttributes.FRUIT_EFFECTIVENESS, 0.25F));

    /** Every minor node of this tree, i.e. everything transferable into a minor-transfer slot. */
    public static final Set<String> MINORS = Set.of(
            EXPERT_EATER, PRISTINE_CONDITION, EFFICIENT_STEPS, GLUTTON, PACED_STRIKES, EXTENDER,
            TEMPORAL_BREAKING, PLUSHIE_LOVER, TEMPORAL_SHIELDING, TOUGH_STOMACH, CLOCK_ARTIFICIER,
            PYLON_WHISPERER, GARDENER, FRUITY, ARMORED_EXTRACTORS, THE_DECKLESS, SPEED_DEMON,
            QUICK_SEARCH);

    /** Nodes registered with no behaviour, and why. Kept so ids stay reserved and reportable. */
    public static final Map<String, String> INERT = Map.of(
            EXTRACTION_SUPERVISER, "Extraction vaults are shelved (SCOPING_SYNTHESIS 5-bis #17); no extractor entity exists.",
            ARMORED_EXTRACTORS, "Extraction vaults are shelved (SCOPING_SYNTHESIS 5-bis #17); no extractor entity exists.");

    private WendarrNodes() {
    }

    private static Map<String, StatEntry> basicStats() {
        Map<String, StatEntry> stats = new LinkedHashMap<>();
        stats.put(FRUIT_CONISSOUR, new StatEntry(ModGearAttributes.FRUIT_EFFECTIVENESS, 0.01F));
        stats.put(SPEEDY, new StatEntry(ModGearAttributes.MOVEMENT_SPEED, 0.05F));
        stats.put(HEAVILY_EFFECTED, new StatEntry(ModGearAttributes.EFFECT_DURATION, 0.10F));
        stats.put(SPEEDY_CASTER, new StatEntry(ModGearAttributes.COOLDOWN_REDUCTION, 0.05F));
        return Map.copyOf(stats);
    }

    public static boolean owns(String nodeId) {
        return nodeId != null && nodeId.startsWith("wendarr_");
    }

    /** Points a player has in a Wendarr minor, honouring minor-transfer selection. */
    public static int minorPoints(ServerPlayer player, String nodeId) {
        return GodNodeGate.minorPoints(player, GOD, nodeId);
    }

    public static boolean hasMinor(ServerPlayer player, String nodeId) {
        return minorPoints(player, nodeId) > 0;
    }

    public static boolean hasMajor(ServerPlayer player, String nodeId) {
        return GodNodeGate.isActiveMajor(player, GOD, nodeId);
    }

    /** Raw ledger points, ignoring which god is equipped. Only the carryover paths use this. */
    public static int ledgerPoints(ServerPlayer player, String nodeId) {
        if (player.getServer() == null) {
            return 0;
        }
        return GodAlignmentData.get(player.getServer()).getPointsIn(player.getUUID(), GOD, nodeId);
    }

    /** A stat row: one gear attribute and the value one spent point contributes. */
    public record StatEntry(VaultGearAttribute<Float> attribute, float perPoint) {
    }
}

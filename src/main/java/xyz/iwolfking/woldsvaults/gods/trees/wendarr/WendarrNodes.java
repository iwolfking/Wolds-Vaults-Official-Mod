package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/**
 * The Wendarr (The Timekeeper) effect ids Java has to name, sheet rows r63-r88, and the gate reads
 * its handlers, listeners and mixins make.
 *
 * <p>The tuned numbers behind these ids live in {@code god_node_effects_wendarr.json} and are read
 * through {@link WendarrNodeHandlers#params}, never from a Java constant. The five plain stat
 * effects bind the shared {@code gear_attribute_scaled} type and are config alone, which is the
 * shape a new stat node is expected to take.
 *
 * <p>Liveness is answered by {@link GodNodeGate} behind the shared once-a-second cache, which is
 * what retires this tree's own gate reads. One call covers both kinds of node: a minor counts when
 * Wendarr is the active tree or when it is bound to a transfer slot of whichever tree is, and a
 * major only on the active tree - the node type comes from the registry, so a caller no longer
 * picks between a minor reader and a major one.
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

    private WendarrNodes() {
    }

    /** The registry key a node's global damage factor or vault clock rate factor is stored under. */
    public static ResourceLocation key(String effectId) {
        return WoldsVaults.id(effectId);
    }

    /** Effective points the player holds in a Wendarr effect right now, gated and cached. */
    public static int points(ServerPlayer player, String effectId) {
        return GodNodeGate.points(player, GOD, effectId);
    }

    public static boolean isActive(ServerPlayer player, String effectId) {
        return points(player, effectId) > 0;
    }

    /**
     * Raw ledger read, ignoring which god is active. Only the piety source needs this: piety is
     * summed outside the attribute fold and applies its own carryover scale.
     */
    public static int investedPoints(ServerPlayer player, String effectId) {
        return GodVaultUtil.investedPoints(player, GOD, effectId);
    }
}

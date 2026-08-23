package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/** Wendarr (The Timekeeper) node effect ids; tuned values in {@code god_node_effects_wendarr.json}. */
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

    /** Raw ledger read, ignoring which god is active. */
    public static int investedPoints(ServerPlayer player, String effectId) {
        return GodVaultUtil.investedPoints(player, GOD, effectId);
    }
}

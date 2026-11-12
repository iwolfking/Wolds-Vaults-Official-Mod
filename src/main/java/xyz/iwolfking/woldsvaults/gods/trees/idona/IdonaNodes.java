package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/** Idona node effect ids; tuned values in {@code god_node_effects_idona.json}. */
public final class IdonaNodes {
    public static final VaultGod GOD = VaultGod.IDONA;

    public static final String HARD_HITTER = "idona_hard_hitter";
    public static final String HARD_HITTER_II = "idona_hard_hitter_ii";
    public static final String ELITE_CASTER = "idona_elite_caster";
    public static final String ELITE_CASTER_II = "idona_elite_caster_ii";
    public static final String PIOUS_DEVOTION = "idona_pious_devotion";
    public static final String KINETIC_IMPACT = "idona_kinetic_impact";
    public static final String SURROUNDED = "idona_surrounded";
    public static final String GRAND_ARCHMAGE = "idona_grand_archmage";
    public static final String ULTRA_RAMPAGING = "idona_ultra_rampaging";
    public static final String OVERCRIT = "idona_overcrit";
    public static final String TRUE_RAGE = "idona_true_rage";
    public static final String CRUSHING_BLOWS = "idona_crushing_blows";
    public static final String UNDER_PRESSURE = "idona_under_pressure";
    public static final String PINCUSHION = "idona_pincushion";
    public static final String BANKED_ANGER = "idona_banked_anger";
    public static final String SOULSTEALER = "idona_soulstealer";
    public static final String FULL_OF_SOUL = "idona_full_of_soul";
    public static final String LUCKIEST_HIT = "idona_luckiest_hit";
    public static final String THWACK = "idona_thwack";
    public static final String WEAPONMASTER = "idona_weaponmaster";
    public static final String CLEAVE_EXPERT = "idona_cleave_expert";
    public static final String FORTUNATE = "idona_fortunate";
    public static final String FORTUNATE_II = "idona_fortunate_ii";
    public static final String SNEAKY_ADVANTAGE = "idona_sneaky_advantage";
    public static final String SUPER_STACKER = "idona_super_stacker";
    public static final String STACK_HOARDER = "idona_stack_hoarder";
    public static final String PRISON_WARDEN = "idona_prison_warden";
    public static final String STACK_STACK_STACK = "idona_stack_stack_stack";
    public static final String GREEDBANE = "idona_greedbane";
    public static final String KING_HUNTER = "idona_king_hunter";
    public static final String ENFORCER = "idona_enforcer";
    public static final String POWER_DUMP = "idona_power_dump";

    private IdonaNodes() {
    }

    public static ResourceLocation key(String effectId) {
        return WoldsVaults.id(effectId);
    }

    /** Effective points the player holds in an Idona effect right now, gated and cached. */
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

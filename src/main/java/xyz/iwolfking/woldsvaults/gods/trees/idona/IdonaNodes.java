package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

import java.util.List;
import java.util.Set;

/**
 * Node identity for the Idona god tree: the id of every node, the per-point value table behind
 * each one, and the gates the handlers use to decide whether a node is live for a player.
 *
 * <p>A stat the sheet lists as a pair ({@code 25%+, 50%+}) is two ids, not one node with two
 * ranks: the shallow placements in the tree carry the base id at the first value and the deep
 * ones carry the {@code _ii} id at the second, each paying its own value per star with no
 * ceiling. The pairs are not always linear, which is why they are two tables and not a
 * multiplier (Fortunate is 2.5% then 4%). Which placement is which is decided by depth in
 * {@code tree-drafts/export_idona_bands.py}, not here.
 */
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

    /** Stat nodes -  the only ones that carry over to a foreign tree at 25%. */
    public static final Set<String> STAT_NODES = Set.of(
            HARD_HITTER, HARD_HITTER_II, ELITE_CASTER, ELITE_CASTER_II, PIOUS_DEVOTION,
            FULL_OF_SOUL, FORTUNATE, FORTUNATE_II, STACK_STACK_STACK, KING_HUNTER, ENFORCER);

    /** Minor nodes -  the transferable set, resolvable through a foreign god's transfer slots. */
    public static final Set<String> MINOR_NODES = Set.of(
            KINETIC_IMPACT, SURROUNDED, OVERCRIT, TRUE_RAGE, CRUSHING_BLOWS, UNDER_PRESSURE,
            PINCUSHION, BANKED_ANGER, SOULSTEALER, LUCKIEST_HIT, THWACK, WEAPONMASTER,
            CLEAVE_EXPERT, SNEAKY_ADVANTAGE, SUPER_STACKER, STACK_HOARDER, PRISON_WARDEN,
            GREEDBANE, POWER_DUMP);

    /** Major nodes -  active only while Idona is the equipped god. */
    public static final Set<String> MAJOR_NODES = Set.of(GRAND_ARCHMAGE);

    public static final List<String> ALL_NODES = List.of(
            HARD_HITTER, ELITE_CASTER, PIOUS_DEVOTION, KINETIC_IMPACT, SURROUNDED, GRAND_ARCHMAGE,
            OVERCRIT, TRUE_RAGE, CRUSHING_BLOWS, UNDER_PRESSURE, PINCUSHION, BANKED_ANGER,
            SOULSTEALER, FULL_OF_SOUL, LUCKIEST_HIT, THWACK, WEAPONMASTER, CLEAVE_EXPERT,
            FORTUNATE, SNEAKY_ADVANTAGE, SUPER_STACKER, STACK_HOARDER, PRISON_WARDEN,
            STACK_STACK_STACK, GREEDBANE, KING_HUNTER, ENFORCER, POWER_DUMP);

    static final float[] V_HARD_HITTER = {0.25F};
    static final float[] V_HARD_HITTER_II = {0.50F};
    static final float[] V_ELITE_CASTER = {0.25F};
    static final float[] V_ELITE_CASTER_II = {0.50F};
    static final float[] V_PIOUS_DEVOTION = {10.0F};
    static final float[] V_FULL_OF_SOUL = {0.50F};
    static final float[] V_FORTUNATE = {0.025F};
    static final float[] V_FORTUNATE_II = {0.04F};
    static final float[] V_STACK_STACK_STACK = {1.0F};
    static final float[] V_KING_HUNTER = {0.75F};
    static final float[] V_ENFORCER = {0.75F};

    static final float KINETIC_IMPACT_PER_PERCENT = 0.001F;
    static final float SURROUNDED_PER_MOB = 0.025F;
    static final double SURROUNDED_RADIUS = 20.0D;
    static final float CRUSHING_BLOWS_MULTIPLIER = 1.15F;
    static final int UNDER_PRESSURE_WINDOW_TICKS = 15 * 60 * 20;
    static final float UNDER_PRESSURE_MAX = 2.0F;
    static final float PINCUSHION_PER_HIT = 0.005F;
    static final double BANKED_ANGER_BASE = 100000.0D;
    static final float SOULSTEALER_MULTIPLIER = 1.5F;
    static final float LUCKIEST_HIT_CHANCE_SCALE = 0.1F;
    static final float THWACK_MULTIPLIER = 2.0F;
    static final float WEAPONMASTER_TWO_HANDED = 1.5F;
    static final double WEAPONMASTER_DUAL_WIELD_ATTACK_SPEED = 0.3D;
    static final float CLEAVE_EXPERT_EFFICIENCY = 0.2F;
    static final float TRUE_RAGE_EFFICIENCY = 0.5F;
    static final float SNEAKY_ADVANTAGE_PER_EFFECT = 0.05F;
    static final float SUPER_STACKER_MULTIPLIER = 1.25F;
    static final float STACK_HOARDER_MULTIPLIER = 1.5F;
    static final float PRISON_WARDEN_MULTIPLIER = 2.0F;
    static final int PRISON_WARDEN_DURATION_TICKS = 30 * 20;
    static final float GREEDBANE_MULTIPLIER = 1.5F;
    static final float POWER_DUMP_PER_MANA = 0.0025F;
    static final int POWER_DUMP_SURPLUS_TTL_TICKS = 45 * 20;
    static final int POWER_DUMP_CONTINUOUS_GRACE_TICKS = 30;

    static final float ARCHMAGE_MANA_PERCENTILE = 1.0F;
    static final int ARCHMAGE_MANA_FLAT = 200;
    static final float ARCHMAGE_MANA_REGEN = 8.0F;
    static final float ARCHMAGE_ABILITY_DAMAGE = 2.0F;
    static final float ARCHMAGE_CDR_CAP = 0.05F;

    private IdonaNodes() {
    }

    /** The registry key a node's global damage factor is stored under. */
    public static ResourceLocation key(String nodeId) {
        return WoldsVaults.id(nodeId);
    }

    /**
     * Total value of a two-value stat node at {@code points}. Values below the table are read
     * directly; values above it extrapolate by the last listed increment.
     */
    public static float valueAt(float[] table, int points) {
        if (points <= 0 || table.length == 0) {
            return 0.0F;
        }
        if (points <= table.length) {
            return table[points - 1];
        }
        float last = table[table.length - 1];
        float step = table.length == 1 ? last : last - table[table.length - 2];
        return last + step * (points - table.length);
    }

    /** Points a player has in a minor node, honouring minor-transfer slots on a foreign god. */
    public static int minorPoints(ServerPlayer player, String nodeId) {
        return GodNodeGate.minorPoints(player, GOD, nodeId);
    }

    public static boolean isMinorActive(ServerPlayer player, String nodeId) {
        return GodNodeGate.isActiveMinor(player, GOD, nodeId);
    }

    /** Points a player has in a major node; majors never transfer and never carry over. */
    public static int majorPoints(ServerPlayer player, String nodeId) {
        return GodNodeGate.activePoints(player, GOD, nodeId);
    }

    public static boolean isMajorActive(ServerPlayer player, String nodeId) {
        return GodNodeGate.isActiveMajor(player, GOD, nodeId);
    }

    /** Raw ledger points, ignoring which god is equipped. Only the carryover paths use this. */
    public static int ledgerPoints(ServerPlayer player, String nodeId) {
        if (player.getServer() == null) {
            return 0;
        }
        return GodAlignmentData.get(player.getServer()).getPointsIn(player.getUUID(), GOD, nodeId);
    }
}

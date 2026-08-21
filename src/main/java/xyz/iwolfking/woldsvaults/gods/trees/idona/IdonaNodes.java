package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

import java.util.List;
import java.util.Set;

/**
 * Node identity for the Idona god tree: the id of every node, the accessors that read its tuned
 * numbers out of {@code god_node_effects_idona.json}, and the gates the handlers use to decide
 * whether a node is live for a player.
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

    private IdonaNodes() {
    }

    static float kineticImpactPerPercent() {
        return GodNodeValues.number(KINETIC_IMPACT, "per_percent");
    }

    static float surroundedPerMob() {
        return GodNodeValues.number(SURROUNDED, "per_mob");
    }

    static double surroundedRadius() {
        return GodNodeValues.precise(SURROUNDED, "radius");
    }

    static float crushingBlowsMultiplier() {
        return GodNodeValues.number(CRUSHING_BLOWS, "multiplier");
    }

    static int underPressureWindowTicks() {
        return GodNodeValues.count(UNDER_PRESSURE, "window_ticks");
    }

    static float underPressureMax() {
        return GodNodeValues.number(UNDER_PRESSURE, "max");
    }

    static float pincushionPerHit() {
        return GodNodeValues.number(PINCUSHION, "per_hit");
    }

    static double bankedAngerBase() {
        return GodNodeValues.precise(BANKED_ANGER, "base");
    }

    static float soulstealerMultiplier() {
        return GodNodeValues.number(SOULSTEALER, "multiplier");
    }

    static float luckiestHitChanceScale() {
        return GodNodeValues.number(LUCKIEST_HIT, "chance_scale");
    }

    static float thwackMultiplier() {
        return GodNodeValues.number(THWACK, "multiplier");
    }

    static float weaponmasterTwoHanded() {
        return GodNodeValues.number(WEAPONMASTER, "two_handed");
    }

    static double weaponmasterDualWieldAttackSpeed() {
        return GodNodeValues.precise(WEAPONMASTER, "dual_wield_attack_speed");
    }

    static float cleaveExpertEfficiency() {
        return GodNodeValues.number(CLEAVE_EXPERT, "efficiency");
    }

    static float trueRageEfficiency() {
        return GodNodeValues.number(TRUE_RAGE, "efficiency");
    }

    static float sneakyAdvantagePerEffect() {
        return GodNodeValues.number(SNEAKY_ADVANTAGE, "per_effect");
    }

    static float superStackerMultiplier() {
        return GodNodeValues.number(SUPER_STACKER, "multiplier");
    }

    static float stackHoarderMultiplier() {
        return GodNodeValues.number(STACK_HOARDER, "multiplier");
    }

    static float prisonWardenMultiplier() {
        return GodNodeValues.number(PRISON_WARDEN, "multiplier");
    }

    static int prisonWardenDurationTicks() {
        return GodNodeValues.count(PRISON_WARDEN, "duration_ticks");
    }

    static float greedbaneMultiplier() {
        return GodNodeValues.number(GREEDBANE, "multiplier");
    }

    static float powerDumpPerMana() {
        return GodNodeValues.number(POWER_DUMP, "per_mana");
    }

    static int powerDumpSurplusTtlTicks() {
        return GodNodeValues.count(POWER_DUMP, "surplus_ttl_ticks");
    }

    static int powerDumpContinuousGraceTicks() {
        return GodNodeValues.count(POWER_DUMP, "continuous_grace_ticks");
    }

    static float archmageManaPercentile() {
        return GodNodeValues.number(GRAND_ARCHMAGE, "mana_percentile");
    }

    static int archmageManaFlat() {
        return GodNodeValues.count(GRAND_ARCHMAGE, "mana_flat");
    }

    static float archmageManaRegen() {
        return GodNodeValues.number(GRAND_ARCHMAGE, "mana_regen");
    }

    static float archmageAbilityDamage() {
        return GodNodeValues.number(GRAND_ARCHMAGE, "ability_damage");
    }

    static float archmageCdrCap() {
        return GodNodeValues.number(GRAND_ARCHMAGE, "cooldown_reduction_cap");
    }

    /** The registry key a node's global damage factor is stored under. */
    public static ResourceLocation key(String nodeId) {
        return WoldsVaults.id(nodeId);
    }

    /**
     * Total value of a two-value stat node at {@code points}, read from the effect's configured
     * per-point table. Values below the table are read directly; values above it extrapolate by
     * the last listed increment.
     */
    public static float valueAt(String effectId, int points) {
        float[] table = GodNodeValues.table(effectId);
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

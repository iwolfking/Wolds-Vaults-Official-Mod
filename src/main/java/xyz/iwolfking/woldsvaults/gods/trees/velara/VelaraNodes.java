package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/** Velara node effect ids. Only effects with behaviour appear here; stat effects are config alone. */
public final class VelaraNodes {
    public static final VaultGod GOD = VaultGod.VELARA;

    public static final String PIOUS_DEVOTION = "velara_pious_devotion";
    public static final String COUNTERSTRIKE = "velara_counterstrike";
    public static final String MAGIC_ARMOR = "velara_magic_armor";
    public static final String DEFENDER_OF_THE_FAITH = "velara_defender_of_the_faith";
    public static final String SACRIFICE = "velara_sacrifice";
    public static final String PERSERVERENCE = "velara_perserverence";
    public static final String ADAPTIVE_ARMOR = "velara_adaptive_armor";
    public static final String BOUNCE_BACK = "velara_bounce_back";
    public static final String INDOMITABLE = "velara_indomitable";
    public static final String FIELD_MEDIC = "velara_field_medic";
    public static final String THE_STONEWALL = "velara_the_stonewall";
    public static final String CACTUS = "velara_cactus";
    public static final String MALEDICTION = "velara_malediction";
    public static final String IMMORTAL = "velara_immortal";
    public static final String FLEETING_PHYSICALITY = "velara_fleeting_physicality";
    public static final String STEADFAST = "velara_steadfast";
    public static final String SANITATION = "velara_sanitation";
    public static final String PRESENCE = "velara_presence";
    public static final String HEALING_FLOW = "velara_healing_flow";
    public static final String UTILIZED = "velara_utilized";

    private VelaraNodes() {
    }

    /** Effective points the player holds in a Velara effect right now, gated and cached. */
    public static int points(ServerPlayer player, String effectId) {
        return GodNodeGate.points(player, GOD, effectId);
    }

    public static boolean isActive(ServerPlayer player, String effectId) {
        return points(player, effectId) > 0;
    }

    public static boolean isActive(Entity entity, String effectId) {
        return entity instanceof ServerPlayer player && isActive(player, effectId);
    }

    /** Raw ledger read, ignoring which god is active. */
    public static int investedPoints(ServerPlayer player, String effectId) {
        return GodVaultUtil.investedPoints(player, GOD, effectId);
    }
}

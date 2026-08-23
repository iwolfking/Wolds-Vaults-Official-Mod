package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/** Tenos node effect ids; tuned values in {@code god_node_effects_tenos.json}. */
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

    private TenosNodes() {
    }

    public static ResourceLocation key(String effectId) {
        return WoldsVaults.id(effectId);
    }

    /** Effective points the player holds in a Tenos effect right now, gated and cached. */
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

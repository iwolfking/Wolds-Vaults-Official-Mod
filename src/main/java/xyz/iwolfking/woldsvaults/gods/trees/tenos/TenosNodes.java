package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

/**
 * The Tenos (The Omniscient) effect ids Java has to name, sheet rows r92-r118, and the gate reads
 * its handlers, listeners and mixins make.
 *
 * <p>The tuned numbers behind these ids live in {@code god_node_effects_tenos.json} and are read
 * through {@link TenosNodeHandlers#params}, never from a Java constant. A stat the sheet lists as a
 * pair ({@code 25%+, 50%+}) is two ids, not one node with two ranks: the shallow placements are the
 * base id at the first value and the deep ones are the {@code _ii} id at the second. Both bands are
 * plain {@code gear_attribute_scaled} effects and are config alone, so nothing here needs to know
 * about banding; which placement is which is decided by depth in
 * {@code tree-drafts/export_tenos_wiring.py}.
 *
 * <p>Liveness is answered by {@link GodNodeGate} behind the shared once-a-second cache, which is
 * what retires this tree's own gate reads - including the four uncached ones the item quantity and
 * item rarity stat listeners used to make on every single read. One call covers both kinds of node:
 * a minor counts when Tenos is the active tree or when it is bound to a transfer slot of whichever
 * tree is, and a major only on the active tree - the node type comes from the registry, so a caller
 * no longer picks between a minor reader and a major one.
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

    private TenosNodes() {
    }

    /** The registry key a node's global damage factor or vault modifier is stored under. */
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

    /**
     * Raw ledger read, ignoring which god is active. Only the piety source needs this: piety is
     * summed outside the attribute fold and applies its own carryover scale.
     */
    public static int investedPoints(ServerPlayer player, String effectId) {
        return GodVaultUtil.investedPoints(player, GOD, effectId);
    }
}

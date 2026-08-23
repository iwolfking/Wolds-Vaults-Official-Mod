package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.init.ModGearAttributes;

import net.minecraft.world.item.ItemStack;

import xyz.iwolfking.woldsvaults.gods.GodFocusGear;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.ListenerBoundHandler;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;

/**
 * Every handler type the Tenos tree owns, and the typed parameters each one reads.
 *
 * <p>Fourteen plain stat effects bind the shared {@code gear_attribute_scaled} type and Pious
 * Devotion binds the shared {@code piety} type, so none of those is named here. What is left is one
 * type per effect Java has to know about: the two stat effects that pay a non-float attribute or
 * read live gear, the vault and ticker effects in {@link TenosVaultHandlers}, and the rest as
 * {@link ListenerBoundHandler} - numbers in config, validated at load, behaviour still reached through the
 * base mod events, the shared final damage stage and the mixins that own the ordering.
 *
 * <p>Params component names are the config keys verbatim, including their underscores. The codec
 * binds by component name, so renaming one here silently means renaming it in the config file
 * too.
 */
public final class TenosNodeHandlers {
    /**
     * Global Veins targets the {@code Vein_Miner} ability, so its levels reach all five
     * specialisations; only chain miner has tiers past its base cap to reach.
     */
    public static final String VEIN_MINER_ABILITY = "Vein_Miner";

    private TenosNodeHandlers() {
    }

    /**
     * Registers every Tenos handler type. Called from {@code GodNodeHandlerTypes.bootstrap()},
     * which is the only point guaranteed to run before the god tree configs are validated.
     */
    public static void register() {
        GodNodeHandlers.register(TenosNodes.GLOBAL_VEINS, GlobalVeinsParams.class, GlobalVeinsHandler::new);
        GodNodeHandlers.register(TenosNodes.SACKED, SackedHandler::new);
        GodNodeHandlers.register(TenosNodes.OMEGA_VAULT, TenosVaultHandlers.OmegaVaultHandler::new);
        GodNodeHandlers.register(TenosNodes.MASTER_OF_CHESTS, TenosVaultHandlers.MasterOfChestsHandler::new);
        GodNodeHandlers.register(TenosNodes.CHALLENGE_TACKLER, ChallengeTacklerParams.class,
                TenosVaultHandlers.ChallengeTacklerHandler::new);
        GodNodeHandlers.register(TenosNodes.SACK_OF_MOBS, SackOfMobsParams.class,
                TenosVaultHandlers.SackOfMobsHandler::new);
        GodNodeHandlers.register(TenosNodes.UNSTOPPABLE_GREED, UnstoppableGreedParams.class,
                TenosVaultHandlers.UnstoppableGreedHandler::new);
        GodNodeHandlers.register(TenosNodes.GOLD_PLATING, GoldPlatingParams.class,
                TenosVaultHandlers.GoldPlatingHandler::new);
        GodNodeHandlers.register(TenosNodes.DEEP_RESERVES, DeepReservesParams.class,
                TenosVaultHandlers.DeepReservesHandler::new);
        GodNodeHandlers.register(TenosNodes.LOOTING_ENGINE, LootingEngineParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.MANA_STARVED, ManaStarvedParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.BARTER_EXPERT, BarterExpertParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.NOSE_FOR_TREASURE, NoseForTreasureParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.DOMAIN_EXPANSION, DomainExpansionParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.MASSIVE_CHESTS, MassiveChestsParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.INDIANA_JONES, IndianaJonesParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.EXPERT_LOOTER, ExpertLooterParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.DRILLMASTER, DrillmasterParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.WEALTHY_PATRON, WealthyPatronParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(TenosNodes.CASH_HUNTER, CashHunterParams.class, ListenerBoundHandler::new);
        GodNodePreviews.register(TenosNodes.LOOTING_ENGINE, GodNodePreviews.LOOTING_ENGINE_FORMULA,
                TenosLootStats::previewLootingEngine);
        GodNodePreviews.register(TenosNodes.INDIANA_JONES, GodNodePreviews.INDIANA_JONES_FORMULA,
                TenosLootStats::previewIndianaJones);
        GodNodePreviews.register(TenosNodes.SACK_OF_MOBS, GodNodePreviews.SACK_OF_MOBS_FORMULA,
                TenosSackOfMobs::preview);
    }

    /**
     * The loaded parameters of one Tenos effect. Absence is a programming error rather than a
     * config error - load-time validation has already asserted that every placed effect exists
     * and resolves its handler - so it fails loudly naming the effect instead of degrading to a
     * zero that would read as a balance change.
     */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        return GodNodeRegistry.params(VaultGod.TENOS, effectId, type);
    }

    /**
     * Global Veins: added ability levels on the vein miner ability, which is exactly what the base
     * mod's added-ability-level attribute already expresses. The value is an
     * {@link AbilityLevelAttribute} rather than a float, which is why it does not bind the shared
     * scaled type. Reaching the raised chain-miner tiers is gated separately in
     * {@link TenosAbilityCaps}.
     */
    public record GlobalVeinsHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            int levels = this.effect.params(GlobalVeinsParams.class).levels() * context.points();
            if (levels == 0) {
                return;
            }
            sink.add(VaultGearAttributeInstance.cast(ModGearAttributes.ABILITY_LEVEL,
                    new AbilityLevelAttribute(VEIN_MINER_ABILITY, levels)));
        }
    }

    /**
     * Sacked doubles an offhand loot sack by contributing a second copy of every attribute the sack
     * already rolls, which lands on the snapshot exactly like the gear pass did. Read from the live
     * offhand at snapshot time, so it cannot double anything the player is not actually holding.
     */
    public record SackedHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            ItemStack offhand = context.player().getOffhandItem();
            if (offhand.isEmpty() || !(offhand.getItem() instanceof VaultLootSackItem)) {
                return;
            }
            GodFocusGear.copyAttributes(offhand, sink);
        }
    }

    public record GlobalVeinsParams(int levels) implements GodEffectParams {
    }

    public record ChallengeTacklerParams(float extra_crate_tier_ratio) implements GodEffectParams {
    }

    public record SackOfMobsParams(float log_base) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.log_base <= 1.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has log_base " + this.log_base
                        + "; it is the base of a logarithm and must be greater than 1");
            }
        }
    }

    public record UnstoppableGreedParams(float ratio) implements GodEffectParams {
    }

    public record GoldPlatingParams(float damage_multiplier, float cost_coefficient,
                                    float cost_offset) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.cost_offset <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has cost_offset " + this.cost_offset
                        + "; it is the argument of a logarithm and must be greater than zero");
            }
        }
    }

    public record DeepReservesParams(float multiplier) implements GodEffectParams {
    }

    public record LootingEngineParams(float reference) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.reference <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has reference " + this.reference
                        + "; it is a divisor and must be greater than zero");
            }
        }
    }

    public record ManaStarvedParams(float max_bonus, float threshold) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.threshold <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has threshold " + this.threshold
                        + "; it is a divisor and must be greater than zero");
            }
        }
    }

    public record BarterExpertParams(float rich_palette_chance) implements GodEffectParams {
    }

    public record NoseForTreasureParams(double treasure_door_bonus) implements GodEffectParams {
    }

    public record DomainExpansionParams(float per_cell, float cap) implements GodEffectParams {
    }

    public record MassiveChestsParams(int rolls, float rarity_multiplier) implements GodEffectParams {
    }

    public record IndianaJonesParams(float reference) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.reference <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has reference " + this.reference
                        + "; it is a divisor and must be greater than zero");
            }
        }
    }

    public record ExpertLooterParams(int rolls) implements GodEffectParams {
    }

    public record DrillmasterParams(int raised_fortune_cap) implements GodEffectParams {
    }

    public record WealthyPatronParams(float per_unique) implements GodEffectParams {
    }

    public record CashHunterParams(float efficiency) implements GodEffectParams {
    }
}

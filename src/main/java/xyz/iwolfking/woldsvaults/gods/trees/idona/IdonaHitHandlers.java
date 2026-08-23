package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.gods.combat.RampageAccess;
import xyz.iwolfking.woldsvaults.gods.node.CombatContributor;
import xyz.iwolfking.woldsvaults.gods.node.GodCombatPipeline;
import xyz.iwolfking.woldsvaults.gods.node.GodDamageContext;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;

/**
 * The Idona nodes whose multiplier depends on the hit itself - the target, the weapon or the code
 * path the damage arrived through - and therefore cannot be expressed as a per-player factor in
 * {@link xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry}.
 *
 * <p>All of them are {@link CombatContributor}s on the pre-mitigation leg of
 * {@link GodCombatPipeline}, which occupies the same {@code LOW} {@link LivingHurtEvent} seam
 * these nodes were applied at before: the bonus still passes through the target's armour and
 * resistance like every other damage bonus in the game, and it composes multiplicatively with the
 * global registry instead of racing it. Percentage-based damage is resolved once by the pipeline
 * and skipped here for the same reason the registry skips it.
 *
 * <p>What is left on Forge listeners is the two things no capability seam can express: cancelling
 * a hit outright, and reading a mob effect leaving a target.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaHitHandlers {
    private IdonaHitHandlers() {
    }

    /**
     * Grand Archmage's downside: the player loses weapon swings entirely. Cancelling the hurt
     * event is the only mechanism the codebase offers - there is no "base attack damage" hook -
     * so on-hit procs that ride the same swing are suppressed with it. Thorns, tridents,
     * boomerangs and every ability are untouched, because {@code isNormalAttack} already excludes
     * them. It stays a listener rather than a contributor because the combat pipeline composes a
     * running amount and has no way to cancel a hit.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void suppressArchmageSwings(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!WoldEventHelper.isNormalAttack() || event.getSource().getDirectEntity() != player) {
            return;
        }
        if (IdonaNodes.isActive(player, IdonaNodes.GRAND_ARCHMAGE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPrisonRemoved(PotionEvent.PotionRemoveEvent event) {
        markIfSurvived(event.getEntityLiving(), event.getPotion());
    }

    @SubscribeEvent
    public static void onPrisonExpired(PotionEvent.PotionExpiryEvent event) {
        markIfSurvived(event.getEntityLiving(), event.getPotionEffect() == null ? null : event.getPotionEffect().getEffect());
    }

    /**
     * There is no event for a glacial prison breaking, so the mark is taken from the effect
     * leaving a living target: the addon's shatter overwrite removes the effect from anything that
     * survives its burst, and an expiry means the prison ran out without shattering the mob.
     */
    private static void markIfSurvived(LivingEntity entity, net.minecraft.world.effect.MobEffect effect) {
        if (entity == null || effect != ModEffects.GLACIAL_SHATTER || entity.level.isClientSide() || !entity.isAlive()) {
            return;
        }
        IdonaState.markPrisonSurvivor(entity.getId(), entity.level.getGameTime());
    }

    /**
     * Pincushion. Only real swings advance the counter: counting every damage instance would let
     * echo ticks, proc fangs and damage-over-time inflate it by an order of magnitude - the
     * failure mode the damage-overflow investigation documented.
     */
    public record PincushionHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.isPercentageBased() || !WoldEventHelper.isNormalAttack()) {
                return;
            }
            int priorHits = IdonaState.recordPincushionHit(context.player(), damage.getTarget().getId());
            if (priorHits <= 0) {
                return;
            }
            damage.multiply(1.0F + this.effect.params(IdonaNodeHandlers.PincushionParams.class).per_hit()
                    * priorHits * context.points());
        }
    }

    /** Sneaky Advantage: more damage per distinct harmful effect already on the target. */
    public record SneakyAdvantageHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.isPercentageBased()) {
                return;
            }
            int effects = IdonaTargeting.countNegativeEffects(damage.getTarget());
            if (effects <= 0) {
                return;
            }
            damage.multiply(1.0F + this.effect.params(IdonaNodeHandlers.SneakyAdvantageParams.class).per_effect()
                    * effects * context.points());
        }
    }

    /** Greedbane: a flat multiplier against the greed assassins and champions. */
    public record GreedbaneHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.isPercentageBased()) {
                return;
            }
            LivingEntity target = damage.getTarget();
            if (!IdonaTargeting.isGreedAssassin(target) && !IdonaTargeting.isGreedChampion(target)) {
                return;
            }
            damage.multiply((float) Math.pow(this.effect.params(IdonaNodeHandlers.GreedbaneParams.class).multiplier(),
                    context.points()));
        }
    }

    /** Prison Warden: a flat multiplier against anything that survived a glacial prison. */
    public record PrisonWardenHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (damage.isPercentageBased()) {
                return;
            }
            LivingEntity target = damage.getTarget();
            if (!IdonaState.isPrisonSurvivor(target.getId(), target.level.getGameTime())) {
                return;
            }
            damage.multiply((float) Math.pow(this.effect.params(IdonaNodeHandlers.PrisonWardenParams.class).multiplier(),
                    context.points()));
        }
    }

    /**
     * True Rage re-exports the Rampage damage bonus onto a path Rampage deliberately skips.
     * Rampage ignores anything flagged as area damage, which is how attack-damage abilities deal
     * theirs; this node gives that damage a fraction of the bonus back. The cleave sweep is the
     * other such path and belongs to Cleave Expert, so the two guards are complements and never
     * both pay for one hit.
     */
    public record TrueRageHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (IdonaState.isCleaving(context.player())) {
                return;
            }
            rampageExport(context, damage, this.effect.params(IdonaNodeHandlers.TrueRageParams.class).efficiency());
        }
    }

    /** Cleave Expert: the same re-export as True Rage, on the cleave sweep and at its own rate. */
    public record CleaveExpertHandler(GodEffect effect) implements CombatContributor {
        @Override
        public void onOutgoing(GodNodeContext context, GodDamageContext damage) {
            if (!IdonaState.isCleaving(context.player())) {
                return;
            }
            rampageExport(context, damage, this.effect.params(IdonaNodeHandlers.CleaveExpertParams.class).efficiency());
        }
    }

    /**
     * The shared re-export both nodes use.
     *
     * <p>Reads the Rampage bonus through {@link RampageAccess} rather than through
     * {@code PlayerDamageHelper}. Until the_vault 3.21.6 those were the same number, because
     * Rampage registered itself in that registry; it no longer does, and what remains there is
     * Aspect of Berserk, the Berserker archetype and Barbarian's rage. Reading the registry here
     * would re-export the wrong bonus entirely.
     *
     * <p>Because the bonus is read through the ability, Ultra Rampaging's Fury scaling is already
     * folded into it. Both nodes therefore grow with Fury, which is intended.
     */
    private static void rampageExport(GodNodeContext context, GodDamageContext damage, float efficiency) {
        if (damage.isPercentageBased()) {
            return;
        }
        if (!ActiveFlags.IS_AOE_ATTACKING.isSet() || ActiveFlags.IS_AP_ATTACKING.isSet()) {
            return;
        }
        float rampage = RampageAccess.effectiveDamageIncrease(context.player());
        if (rampage <= 0.0F) {
            return;
        }
        damage.multiply(1.0F + rampage * efficiency * context.points());
    }
}

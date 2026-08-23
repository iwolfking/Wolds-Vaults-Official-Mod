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
 * Idona nodes whose multiplier depends on the hit itself: {@link CombatContributor}s on the
 * pre-mitigation leg of {@link GodCombatPipeline}, skipping percentage-based damage.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaHitHandlers {
    private IdonaHitHandlers() {
    }

    /** Grand Archmage's downside: cancels normal weapon swings and the on-hit procs riding them. */
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

    /** Marks a living target as a glacial prison survivor when the shatter effect leaves it. */
    private static void markIfSurvived(LivingEntity entity, net.minecraft.world.effect.MobEffect effect) {
        if (entity == null || effect != ModEffects.GLACIAL_SHATTER || entity.level.isClientSide() || !entity.isAlive()) {
            return;
        }
        IdonaState.markPrisonSurvivor(entity.getId(), entity.level.getGameTime());
    }

    /** Pincushion: more damage per prior hit on the target. Only normal attacks advance the counter. */
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

    /** True Rage: re-exports a fraction of the Rampage bonus onto area damage, excluding cleave. */
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

    /** Applies {@code efficiency} of the Rampage bonus. Only non-ability area attacks qualify. */
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

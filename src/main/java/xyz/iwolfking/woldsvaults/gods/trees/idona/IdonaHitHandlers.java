package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

/**
 * The Idona nodes whose multiplier depends on the hit itself -  the target, the weapon or the code
 * path the damage arrived through -  and therefore cannot be expressed as a per-player factor in
 * {@link GlobalDamageMultiplierRegistry}.
 *
 * <p>All of them are folded into one product and applied once, at the same {@code LOW} priority
 * the global registry runs at, so context-conditional nodes compose with the registry
 * multiplicatively instead of racing it. Percentage-based damage is skipped for the same reason
 * the registry skips it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaHitHandlers {
    private IdonaHitHandlers() {
    }

    /**
     * Grand Archmage's downside: the player loses weapon swings entirely. Cancelling the hurt
     * event is the only mechanism the codebase offers -  there is no "base attack damage" hook - 
     * so on-hit procs that ride the same swing are suppressed with it. Thorns, tridents,
     * boomerangs and every ability are untouched, because {@code isNormalAttack} already excludes
     * them.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void suppressArchmageSwings(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!WoldEventHelper.isNormalAttack() || event.getSource().getDirectEntity() != player) {
            return;
        }
        if (IdonaNodes.isMajorActive(player, IdonaNodes.GRAND_ARCHMAGE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void applyConditionalMultipliers(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (GlobalDamageMultiplierRegistry.isPercentageBased(event.getSource())) {
            return;
        }
        if (ActiveGodResolver.getActiveGod(player).isEmpty()) {
            return;
        }
        LivingEntity target = event.getEntityLiving();
        float product = 1.0F;
        product *= pincushion(player, target);
        product *= sneakyAdvantage(player, target);
        product *= greedbane(player, target);
        product *= prisonWarden(player, target);
        product *= weaponmaster(player);
        product *= rampageExport(player);
        if (product != 1.0F) {
            event.setAmount(event.getAmount() * product);
        }
    }

    /**
     * Only real swings advance the counter. Counting every damage instance would let echo ticks,
     * proc fangs and damage-over-time inflate it by an order of magnitude -  the failure mode the
     * damage-overflow investigation documented.
     */
    private static float pincushion(ServerPlayer player, LivingEntity target) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.PINCUSHION);
        if (points <= 0 || !WoldEventHelper.isNormalAttack()) {
            return 1.0F;
        }
        int priorHits = IdonaState.recordPincushionHit(player, target.getId());
        return 1.0F + IdonaNodes.pincushionPerHit() * priorHits * points;
    }

    private static float sneakyAdvantage(ServerPlayer player, LivingEntity target) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.SNEAKY_ADVANTAGE);
        if (points <= 0) {
            return 1.0F;
        }
        int effects = IdonaTargeting.countNegativeEffects(target);
        return effects <= 0 ? 1.0F : 1.0F + IdonaNodes.sneakyAdvantagePerEffect() * effects * points;
    }

    private static float greedbane(ServerPlayer player, LivingEntity target) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.GREEDBANE);
        if (points <= 0) {
            return 1.0F;
        }
        if (!IdonaTargeting.isGreedAssassin(target) && !IdonaTargeting.isGreedChampion(target)) {
            return 1.0F;
        }
        return (float) Math.pow(IdonaNodes.greedbaneMultiplier(), points);
    }

    private static float prisonWarden(ServerPlayer player, LivingEntity target) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.PRISON_WARDEN);
        if (points <= 0) {
            return 1.0F;
        }
        if (!IdonaState.isPrisonSurvivor(target.getId(), target.level.getGameTime())) {
            return 1.0F;
        }
        return (float) Math.pow(IdonaNodes.prisonWardenMultiplier(), points);
    }

    private static float weaponmaster(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.WEAPONMASTER);
        if (points <= 0 || !WoldEventHelper.isNormalAttack()) {
            return 1.0F;
        }
        if (!IdonaTargeting.isTwoHanded(player.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return 1.0F;
        }
        return (float) Math.pow(IdonaNodes.weaponmasterTwoHanded(), points);
    }

    /**
     * True Rage and Cleave Expert both re-export the base attack-damage multiplier registry onto a
     * path it deliberately skips. The registry ignores anything flagged as area damage, which is
     * how attack-damage abilities and the cleave sweep both deal their damage; these two nodes give
     * that damage a fraction of the multiplier back.
     */
    private static float rampageExport(ServerPlayer player) {
        if (!ActiveFlags.IS_AOE_ATTACKING.isSet() || ActiveFlags.IS_AP_ATTACKING.isSet()) {
            return 1.0F;
        }
        boolean cleaving = IdonaState.isCleaving(player);
        String nodeId = cleaving ? IdonaNodes.CLEAVE_EXPERT : IdonaNodes.TRUE_RAGE;
        int points = IdonaNodes.minorPoints(player, nodeId);
        if (points <= 0) {
            return 1.0F;
        }
        float rage = PlayerDamageHelper.getDamageMultiplier(player, true, false);
        if (rage <= 1.0F) {
            return 1.0F;
        }
        float efficiency = cleaving ? IdonaNodes.cleaveExpertEfficiency() : IdonaNodes.trueRageEfficiency();
        return 1.0F + (rage - 1.0F) * efficiency * points;
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
}

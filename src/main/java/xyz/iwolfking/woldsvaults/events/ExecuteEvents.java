package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Execute: a player's hit on a mob at or below their execute threshold, as a fraction of its max health, kills it.
 *
 * <p>The threshold is the {@code execution_damage} gear stat, which applies on every normal melee hit, plus the
 * Executing Strike bonus on a lucky hit. True bosses take half the threshold. The check uses the target's health
 * before the hit and runs after armour, so no later damage stage can push the lethal amount around.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ExecuteEvents {
    public static final float BOSS_THRESHOLD_FACTOR = 0.5F;
    private static final Map<LivingEntity, Float> LUCKY_HIT_BONUS = new WeakHashMap<>();

    private ExecuteEvents() {
    }

    /** Adds a lucky-hit execute bonus to the hit currently landing on {@code target}. */
    public static void addLuckyHitBonus(LivingEntity target, float bonus) {
        LUCKY_HIT_BONUS.merge(target, bonus, Float::sum);
    }

    /** Whether the target counts as a boss for the halved execute threshold. */
    public static boolean isTrueBoss(LivingEntity target) {
        return target instanceof VaultBoss || target instanceof VaultBossBaseEntity || target instanceof TheVesselEntity;
    }

    /** The execute threshold the player's gear grants on every normal melee hit. */
    public static float getGearThreshold(Player player) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.EXECUTION_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void clearStaleBonus(LivingHurtEvent event) {
        LUCKY_HIT_BONUS.remove(event.getEntityLiving());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void execute(LivingDamageEvent event) {
        LivingEntity target = event.getEntityLiving();
        Float luckyHitBonus = LUCKY_HIT_BONUS.remove(target);
        if (!(event.getSource().getEntity() instanceof Player player) || target instanceof Player || !(target.level instanceof ServerLevel level)) {
            return;
        }

        float threshold = luckyHitBonus == null ? 0.0F : luckyHitBonus;
        if (luckyHitBonus != null || gearExecuteApplies(event)) {
            threshold += getGearThreshold(player);
        }
        if (threshold <= 0.0F) {
            return;
        }
        if (isTrueBoss(target)) {
            threshold *= BOSS_THRESHOLD_FACTOR;
        }
        if (target.getHealth() > threshold * target.getMaxHealth()) {
            return;
        }

        float lethal = target.getHealth() + target.getAbsorptionAmount();
        if (event.getAmount() >= lethal) {
            return;
        }
        event.setAmount(lethal);
        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.2, 0.5, 0.2, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 2.0F);
    }

    private static boolean gearExecuteApplies(LivingDamageEvent event) {
        return WoldEventHelper.isNormalAttack()
                && !WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()
                && !event.getSource().isProjectile();
    }
}

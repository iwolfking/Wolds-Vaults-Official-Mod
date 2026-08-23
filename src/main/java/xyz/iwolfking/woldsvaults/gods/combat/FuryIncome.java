package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.boss.VaultBossEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Every way Fury is earned, plus the drawback it carries. Any hit the player is the true source of
 * earns Fury, while the boost it feeds reaches only direct melee swings.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class FuryIncome {
    private FuryIncome() {
    }

    /** Rune bosses, hyper bosses, the greed trial's Vessel and the Vault Champion. */
    public static boolean isBossType(Entity entity) {
        return entity instanceof VaultBossEntity || entity instanceof TheVesselEntity;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHitDealt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !UltraRampaging.isActive(player)) {
            return;
        }
        if (GlobalDamageMultiplierRegistry.isPercentageBased(event.getSource())) {
            return;
        }
        UltraRampagingConfig config = UltraRampaging.config();
        float gain = config.fury_per_hit();
        if (isBossType(event.getEntityLiving())) {
            gain *= config.fury_boss_hit_multiplier();
        }
        PlayerFuryHelper.add(player, gain);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !UltraRampaging.isActive(player)) {
            return;
        }
        awardKill(player, event.getEntityLiving());
    }

    /** Pays the kill half of the income; the Champion is discarded rather than killed, so it calls in directly. */
    public static void awardKill(ServerPlayer player, LivingEntity victim) {
        if (!UltraRampaging.isActive(player)) {
            return;
        }
        UltraRampagingConfig config = UltraRampaging.config();
        float gain = config.fury_per_kill();
        if (isBossType(victim)) {
            gain *= config.fury_boss_kill_multiplier();
        }
        PlayerFuryHelper.add(player, gain);
    }

    /** Registers the damage-taken reader as a {@link FinalDamageStage} sub-stage after every cap and floor. */
    public static void init() {
        FinalDamageStage.register(WoldsVaults.id("ultra_rampaging_fury"), FinalDamageStage.ORDER_FLOOR + 300,
                (event, amount) -> {
                    awardForDamageTaken(event, amount);
                    return amount;
                });
    }

    /** Pays Fury for the fraction of max health actually lost; absorbed damage pays nothing. */
    private static void awardForDamageTaken(LivingDamageEvent event, float amount) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || !UltraRampaging.isActive(player)) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0.0F) {
            WoldsVaults.LOGGER.error("Skipping Fury for damage taken by {}: max health is {}, which would divide by "
                    + "zero.", player.getGameProfile().getName(), maxHealth);
            return;
        }
        float lost = Math.min(amount, player.getHealth());
        if (lost <= 0.0F) {
            return;
        }
        float fraction = Math.min(lost / maxHealth, 1.0F);
        PlayerFuryHelper.add(player, UltraRampaging.config().fury_per_hp_fraction_lost() * fraction);
    }

    /** Multiplies raw incoming damage by the Fury drawback, ahead of armour, even with Rampage off. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void amplifyIncoming(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || !UltraRampaging.isActive(player)) {
            return;
        }
        float fury = PlayerFuryHelper.get(player);
        if (fury <= 0.0F) {
            return;
        }
        float multiplier = UltraRampaging.incomingMultiplier(fury, UltraRampaging.config());
        if (multiplier > 1.0F) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}

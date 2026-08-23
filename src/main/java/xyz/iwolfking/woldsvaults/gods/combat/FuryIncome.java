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
 * Every way Fury enters and the drawback it carries, in one place.
 *
 * <p>What <em>earns</em> Fury and what the boosted damage <em>applies to</em> are deliberately
 * different sets. Fury is earned by any hit the player is the true source of, cleave and
 * attack-damage abilities included; the boost itself still only reaches the direct, full-charge,
 * non-critical melee swings Rampage has always been gated to. Cleave therefore feeds the swing
 * that feeds cleave, which is the loop the node is built around.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class FuryIncome {
    private FuryIncome() {
    }

    /**
     * Rune bosses, hyper bosses, the greed trial's Vessel and the Vault Champion.
     *
     * <p>A hyper boss is a {@code VaultBossEntity} in a hyper vault, so the class test already
     * covers it and no vault lookup is needed. The Champion is a tagged {@code TheVesselEntity},
     * which the second test covers without having to ask the medallion system anything.
     */
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

    /**
     * The kill half of the income, split out because one of the enemies that should pay it never
     * raises {@link LivingDeathEvent}. The Vault Champion is discarded rather than killed - its
     * fight ends by emptying a damage pool - so {@code VaultChampionEvents} calls this directly
     * from the stage that empties it. Without that call the single most important boss in the
     * rework would silently pay nothing.
     */
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

    /**
     * Registers the damage-taken reader. Called from common setup.
     *
     * <p>A sub-stage of {@link FinalDamageStage} rather than another {@code LOWEST} listener,
     * because that is the one handler in this pack that owns the last word on a hit - a competing
     * listener at the same priority would read a number that depends on class-loading order.
     * Ordered after every reduction, cap and floor, including the Champion's damage pool, so the
     * figure it reads is the health the bar is actually about to lose. It returns the amount
     * untouched; it is a reader, not a stage.
     */
    public static void init() {
        FinalDamageStage.register(WoldsVaults.id("ultra_rampaging_fury"), FinalDamageStage.ORDER_FLOOR + 300,
                (event, amount) -> {
                    awardForDamageTaken(event, amount);
                    return amount;
                });
    }

    /**
     * Fury for damage taken. The fraction is of health actually lost, so armour, resistance and
     * absorption hearts have all already been taken off - absorbed damage is deliberately not paid
     * for, because it never reached the player.
     */
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

    /**
     * The drawback. Raw incoming damage is multiplied before armour and resistance see it, which
     * is what "raw" means here and is why this sits at {@code HIGH} rather than down with the
     * mitigation stages.
     *
     * <p>This and {@link #awardForDamageTaken} form a feedback loop - being hit harder pays more Fury,
     * which makes the next hit harder still. It converges, because Fury income from being hit
     * grows as the square root of Fury while the decay drain is linear in it, but it is the
     * sharpest edge in the node and the reason a player carrying Fury is genuinely easier to kill.
     * Per the node's design that applies even with Rampage toggled off.
     */
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

package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModEffects;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * The hyperboss takes no %-max-health damage-over-time: with hyper-scaled health pools a
 * single Bleed tick (amplifier × 2.5% of max health, see MixinBleedEffect) deals billions.
 * Bleed is denied at application here; Reaving's on-hit proc is neutralized by pre-applying
 * its own once-per-mob latch effect in {@link HyperBossManager}.
 *
 * <p>Also home of the per-hit damage log: every hurt event on a hyperboss logs the raw amount
 * (before any other handler), the final amount (after every handler — reaving/execution-style
 * additions show up as the difference), the damage source, and the attacker's sheet stats.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperBossEffectImmunity {
    // The raw amount seen at HIGHEST priority, consumed by the LOWEST logger of the same event.
    // Hurt events resolve synchronously on the vault's tick thread, so a ThreadLocal pair is
    // enough; the event identity check discards stale values from cancelled events.
    private record RawHit(LivingHurtEvent event, float amount) {
    }

    private static final ThreadLocal<RawHit> RAW_HIT = new ThreadLocal<>();

    private HyperBossEffectImmunity() {
    }

    private static boolean isHyperBoss(LivingEntity entity) {
        return entity instanceof VaultBossEntity
                && ServerVaults.get(entity.level)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
    }

    @SubscribeEvent
    public static void denyBleedOnHyperboss(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.BLEED) {
            return;
        }
        if (isHyperBoss(event.getEntityLiving())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void captureRawBossDamage(LivingHurtEvent event) {
        if (isHyperBoss(event.getEntityLiving())) {
            RAW_HIT.set(new RawHit(event, event.getAmount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logBossDamage(LivingHurtEvent event) {
        LivingEntity boss = event.getEntityLiving();
        if (!isHyperBoss(boss)) {
            return;
        }
        RawHit raw = RAW_HIT.get();
        RAW_HIT.remove();
        String rawAmount = raw != null && raw.event() == event ? String.format("%.1f", raw.amount()) : "?";

        Entity attacker = event.getSource().getEntity();
        String attackerInfo;
        if (attacker instanceof Player player) {
            AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            MobEffectInstance strength = player.getEffect(MobEffects.DAMAGE_BOOST);
            attackerInfo = String.format("player %s [attackDamageAttr=%.1f, strengthAmp=%s, mainHand=%s]",
                    player.getGameProfile().getName(),
                    attack == null ? -1.0 : attack.getValue(),
                    strength == null ? "none" : strength.getAmplifier(),
                    player.getMainHandItem().isEmpty() ? "empty" : player.getMainHandItem().getItem().getRegistryName());
        } else {
            attackerInfo = attacker == null ? "none" : String.valueOf(attacker.getType().getRegistryName());
        }
        Entity immediate = event.getSource().getDirectEntity();

        WoldsVaults.LOGGER.info(
                "Hyperboss hit: raw {} -> final {} ({}% of max) | source={} immediate={} attacker={} | boss {}/{}",
                rawAmount,
                String.format("%.1f", event.getAmount()),
                String.format("%.4f", 100.0F * event.getAmount() / boss.getMaxHealth()),
                event.getSource().getMsgId(),
                immediate == null ? "none" : immediate.getType().getRegistryName(),
                attackerInfo,
                String.format("%.0f", boss.getHealth()),
                String.format("%.0f", boss.getMaxHealth()));
    }
}

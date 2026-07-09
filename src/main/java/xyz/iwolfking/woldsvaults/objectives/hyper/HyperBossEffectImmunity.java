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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
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
 * <p>Also home of the per-hit damage instrumentation: hurt events on a hyperboss are sampled
 * at every event priority so the log shows which priority band multiplied/added what (the
 * Frenzy-family rework in MixinMobFrenzyModifier multiplies ALL player damage per stack, and
 * VH's own gear pipeline runs in these handlers too). A post-armor LivingDamageEvent line
 * closes the chain.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperBossEffectImmunity {
    // Amounts sampled at HIGHEST/HIGH/NORMAL/LOW/LOWEST for one hurt event. Hurt events
    // resolve synchronously on the vault's tick thread; the event identity check discards
    // stale samples from cancelled events.
    private record ChainSample(LivingHurtEvent event, float[] amounts) {
    }

    private static final ThreadLocal<ChainSample> CHAIN = new ThreadLocal<>();
    private static final ThreadLocal<Float> DAMAGE_PHASE_START = new ThreadLocal<>();

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

    // ---- hurt-event chain: one sample per priority band ---------------------------------

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void sampleHighest(LivingHurtEvent event) {
        if (isHyperBoss(event.getEntityLiving())) {
            float[] amounts = {event.getAmount(), Float.NaN, Float.NaN, Float.NaN, Float.NaN};
            CHAIN.set(new ChainSample(event, amounts));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void sampleHigh(LivingHurtEvent event) {
        sample(event, 1);
    }

    @SubscribeEvent
    public static void sampleNormal(LivingHurtEvent event) {
        sample(event, 2);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void sampleLow(LivingHurtEvent event) {
        sample(event, 3);
    }

    private static void sample(LivingHurtEvent event, int index) {
        ChainSample chain = CHAIN.get();
        if (chain != null && chain.event() == event) {
            chain.amounts()[index] = event.getAmount();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logChain(LivingHurtEvent event) {
        LivingEntity boss = event.getEntityLiving();
        if (!isHyperBoss(boss)) {
            return;
        }
        ChainSample chain = CHAIN.get();
        CHAIN.remove();
        float[] a = chain != null && chain.event() == event ? chain.amounts() : null;
        float finalAmount = event.getAmount();

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

        WoldsVaults.LOGGER.info(
                "Hyperboss hurt chain: raw {} | xHIGH {} | xNORMAL {} | xLOW {} | xLOWEST {} -> final {} ({}% of max) | source={} attacker={} | boss {}/{}",
                a == null ? "?" : String.format("%.1f", a[0]),
                bandMultiplier(a, 0, 1), bandMultiplier(a, 1, 2), bandMultiplier(a, 2, 3),
                a == null || Float.isNaN(a[3]) || a[3] == 0.0F ? "?" : String.format("%.2f", finalAmount / a[3]),
                String.format("%.1f", finalAmount),
                String.format("%.4f", 100.0F * finalAmount / boss.getMaxHealth()),
                event.getSource().getMsgId(), attackerInfo,
                String.format("%.0f", boss.getHealth()), String.format("%.0f", boss.getMaxHealth()));
    }

    private static String bandMultiplier(float[] amounts, int from, int to) {
        if (amounts == null || Float.isNaN(amounts[from]) || Float.isNaN(amounts[to]) || amounts[from] == 0.0F) {
            return "?";
        }
        return String.format("%.2f", amounts[to] / amounts[from]);
    }

    // ---- post-armor damage-event chain ---------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void samplePostArmorStart(LivingDamageEvent event) {
        if (isHyperBoss(event.getEntityLiving())) {
            DAMAGE_PHASE_START.set(event.getAmount());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logPostArmor(LivingDamageEvent event) {
        if (!isHyperBoss(event.getEntityLiving())) {
            return;
        }
        Float start = DAMAGE_PHASE_START.get();
        DAMAGE_PHASE_START.remove();
        WoldsVaults.LOGGER.info("Hyperboss post-armor: {} -> {} (x{})",
                start == null ? "?" : String.format("%.1f", start),
                String.format("%.1f", event.getAmount()),
                start == null || start == 0.0F ? "?" : String.format("%.2f", event.getAmount() / start));
    }
}

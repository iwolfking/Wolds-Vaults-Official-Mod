package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.init.ModEffects;
import iskallia.vault.util.PlayerRageHelper;
import iskallia.vault.util.PlayerRelentlessStrikeHelper;
import iskallia.vault.util.calc.FatalStrikeHelper;
import iskallia.vault.util.damage.AttackScaleHelper;
import iskallia.vault.util.damage.CritHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.DamageMultiplierAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PlayerDamageHelperAccessor;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

/**
 * Per-hit damage diagnostics for the hyperboss, kept for future balance forensics but fully
 * gated behind {@code enableDebugMode} (woldsvaults-common.toml): with it off — the shipped
 * default — every handler is a boolean check and normal gameplay logs nothing.
 *
 * <p>With debug mode on, each hyperboss hit logs a hurt-event chain sampled at every priority
 * band (attributing which band multiplied what — VH's own gear pipeline, the frenzy rework and
 * target-side effects all run in different bands), a full dump of the player's live
 * damage-multiplier registry, and a post-armor line. This is the tooling that decoded the
 * playtest damage towers in rounds 22-29.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperBossDamageInstrumentation {
    /**
     * Amounts sampled at HIGHEST/HIGH/NORMAL/LOW/LOWEST for one hurt event. Hurt events
     * resolve synchronously on their vault's tick thread; the event identity check discards
     * stale samples (e.g. an event cancelled before LOWEST ran).
     */
    private record ChainSample(LivingHurtEvent event, float[] amounts) {
    }

    private static final ThreadLocal<ChainSample> CHAIN = new ThreadLocal<>();
    private static final ThreadLocal<Float> DAMAGE_PHASE_START = new ThreadLocal<>();

    private HyperBossDamageInstrumentation() {
    }

    private static boolean off() {
        return !WoldsVaultsConfig.COMMON.enableDebugMode.get();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void sampleHighest(LivingHurtEvent event) {
        if (off()) {
            return;
        }
        if (HyperVaultEvents.isHyperBoss(event.getEntityLiving())) {
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
        if (off()) {
            return;
        }
        ChainSample chain = CHAIN.get();
        if (chain == null) {
            return;
        }
        if (chain.event() != event) {
            CHAIN.remove();
            return;
        }
        chain.amounts()[index] = event.getAmount();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logChain(LivingHurtEvent event) {
        if (off()) {
            return;
        }
        LivingEntity boss = event.getEntityLiving();
        if (!HyperVaultEvents.isHyperBoss(boss)) {
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
        if (attacker instanceof ServerPlayer serverPlayer) {
            logHitDetail(serverPlayer, boss);
        }
    }

    /** The one known fixed id in the damage-multiplier registry (PlayerThirdAttackDamageHelper). */
    private static final UUID THIRD_ATTACK_ID =
            UUID.nameUUIDFromBytes("the_vault:third_attack_damage".getBytes(StandardCharsets.UTF_8));

    /**
     * Every live contributor to the player's damage at the moment of a hyperboss hit:
     * attack charge, VH crit (fatal strike) state and gear stats, every entry in VH's
     * damage-multiplier registry (the xNORMAL hurt-band product), and the rage/retribution
     * stack counters that feed it. One line per logged hit, right under the chain line.
     */
    private static void logHitDetail(ServerPlayer player, LivingEntity boss) {
        try {
            StringBuilder entries = new StringBuilder();
            double additive = 1.0;
            double stacking = 1.0;
            for (PlayerDamageHelper.DamageMultiplier mult : PlayerDamageHelperAccessor.getMultipliers()
                    .getOrDefault(player.getUUID(), Collections.emptyMap()).values()) {
                DamageMultiplierAccessor access = (DamageMultiplierAccessor) (Object) mult;
                boolean isAdditive = access.getOperation() == PlayerDamageHelper.Operation.ADDITIVE_MULTIPLY;
                if (isAdditive) {
                    additive += mult.getMultiplier();
                } else {
                    stacking *= mult.getMultiplier();
                }
                entries.append(String.format(" [%s %s%.2f ttl=%s]",
                        THIRD_ATTACK_ID.equals(access.getId()) ? "third_attack" : access.getId().toString().substring(0, 8),
                        isAdditive ? "+" : "x", mult.getMultiplier(),
                        access.getTickTimeout() == Integer.MAX_VALUE ? "inf" : String.valueOf(access.getTickTimeout())));
            }
            MobEffectInstance vulnerable = boss.getEffect(ModEffects.VULNERABLE);
            WoldsVaults.LOGGER.info(
                    "  hit detail: charge={} fatalStrike={} (chance {} | +{}% dmg) | damageHelper x{} = (1+{}) x {} from:{} | rage {} (x{}/pt) | retribution {} (x{}/pt) | target vulnerableAmp={}",
                    String.format("%.2f", AttackScaleHelper.getLastAttackScale(player)),
                    CritHelper.getCrit(player),
                    String.format("%.2f", FatalStrikeHelper.getFatalStrikeChance(player)),
                    String.format("%.0f", FatalStrikeHelper.getFatalStrikeChance(player) * 100.0F),
                    String.format("%.2f", additive * stacking),
                    String.format("%.2f", additive - 1.0), String.format("%.2f", stacking), entries,
                    PlayerRageHelper.getCurrentRage(player),
                    String.format("%.3f", PlayerRageHelper.getRageDamagePerPoint(player)),
                    PlayerRelentlessStrikeHelper.getCurrentRetribution(player),
                    String.format("%.3f", PlayerRelentlessStrikeHelper.getDamagePerPoint(player)),
                    vulnerable == null ? "none" : vulnerable.getAmplifier());
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Hyperboss hit-detail logging failed", e);
        }
    }

    private static String bandMultiplier(float[] amounts, int from, int to) {
        if (amounts == null || Float.isNaN(amounts[from]) || Float.isNaN(amounts[to]) || amounts[from] == 0.0F) {
            return "?";
        }
        return String.format("%.2f", amounts[to] / amounts[from]);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void samplePostArmorStart(LivingDamageEvent event) {
        if (off()) {
            return;
        }
        if (HyperVaultEvents.isHyperBoss(event.getEntityLiving())) {
            DAMAGE_PHASE_START.set(event.getAmount());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logPostArmor(LivingDamageEvent event) {
        if (off()) {
            return;
        }
        if (!HyperVaultEvents.isHyperBoss(event.getEntityLiving())) {
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

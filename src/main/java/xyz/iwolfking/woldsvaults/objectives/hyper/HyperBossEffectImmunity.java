package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.PlayerRageHelper;
import iskallia.vault.util.PlayerRelentlessStrikeHelper;
import iskallia.vault.util.calc.FatalStrikeHelper;
import iskallia.vault.util.damage.AttackScaleHelper;
import iskallia.vault.util.damage.CritHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.DamageMultiplierAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PlayerDamageHelperAccessor;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

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

    private static boolean isInHyperVault(LivingEntity entity) {
        return ServerVaults.get(entity.level)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
    }

    private static boolean isHyperBoss(LivingEntity entity) {
        return entity instanceof VaultBossEntity && isInHyperVault(entity);
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

    /**
     * The floating lava/void pools are placed as bare liquid source blocks; explosions that
     * eat them (or whatever invisible casing sits around them) spill the pool across the
     * room floor. In hyper vaults explosions therefore cannot affect a pool fluid or any
     * block within one step of one.
     */
    @SubscribeEvent
    public static void protectPoolsFromExplosions(net.minecraftforge.event.world.ExplosionEvent.Detonate event) {
        if (!(event.getWorld() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        if (ServerVaults.get(level)
                .map(vault -> vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(true)) {
            return;
        }
        int before = event.getAffectedBlocks().size();
        event.getAffectedBlocks().removeIf(pos -> isPoolOrCasing(level, pos));
        int removed = before - event.getAffectedBlocks().size();
        if (removed > 0) {
            WoldsVaults.LOGGER.info("Shielded {} pool block(s) from an explosion in a hyper vault.", removed);
        }
    }

    private static boolean isPoolOrCasing(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        if (isPoolFluid(level.getBlockState(pos))) {
            return true;
        }
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            if (isPoolFluid(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPoolFluid(net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock() == net.minecraft.world.level.block.Blocks.LAVA
                || state.getBlock() == iskallia.vault.init.ModBlocks.VOID_LIQUID_BLOCK;
    }

    /**
     * No mob may ever be immortal in a hyper vault. The known offender is VH's elite zombie
     * (a brutal-roster boss): EliteZombieEntity.applySupportEffects self-applies IMMORTALITY
     * every 10 ticks while its raised minions live — and ImmortalityEffect.onAdded carves an
     * explicit exemption for it, so nothing upstream stops it. With hyper-scaled minion
     * health the "kill the minions first" window stretches toward forever. Denying the
     * effect keeps the minion-raising flavor while the boss stays damageable; players are
     * not Mobs and keep any immortality of their own.
     */
    @SubscribeEvent
    public static void denyMobImmortalityInHyperVaults(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.IMMORTALITY) {
            return;
        }
        if (!(event.getEntityLiving() instanceof Mob mob) || !isInHyperVault(mob)) {
            return;
        }
        event.setResult(Event.Result.DENY);
        // The elite zombie retries every 10 ticks; log the first denial per entity only.
        if (mob.addTag("woldsvaults_hyper_immortality_denied")) {
            WoldsVaults.LOGGER.info("Denied Immortality on {} in a hyper vault (mob immortality is disabled here).",
                    mob.getType().getRegistryName());
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
        if (attacker instanceof ServerPlayer serverPlayer) {
            logHitDetail(serverPlayer, boss);
        }
    }

    // The one known fixed id in the damage-multiplier registry (PlayerThirdAttackDamageHelper).
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
                    String.format("%.2f", FatalStrikeHelper.getPlayerFatalStrikeChance(player)),
                    String.format("%.0f", FatalStrikeHelper.getPlayerFatalStrikeDamage(player) * 100.0F),
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

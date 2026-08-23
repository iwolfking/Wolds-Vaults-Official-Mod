package xyz.iwolfking.woldsvaults.medallions.assassins;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;

import java.util.List;
import java.util.Random;

/**
 * The per-tier assassin behaviours. The design sheet fixes none of their magnitudes, so the durations, radii and
 * modifier counts here are interim tuning.
 */
public final class GreedAssassinBehaviors {
    private static final int NEGATIVE_EFFECT_TICKS = 100;
    private static final int AURA_BUFF_TICKS = 120;
    private static final double AURA_RADIUS = 8.0D;
    private static final int INFERNAL_MODIFIER_COUNT = 2;
    private static final MobEffect[] NEGATIVE_EFFECTS = {
            MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS, MobEffects.POISON
    };
    private static final int[] NEGATIVE_EFFECT_AMPLIFIERS = {0, 0, 0, 1};

    private static boolean infernalFailureLogged;

    private GreedAssassinBehaviors() {
    }

    /** Applies the tier-gated additions at spawn; {@code EntityScaler.scale} has already run by this point. */
    public static void applyOnSpawn(Vault vault, GreedMedallionTier tier, LivingEntity assassin) {
        if (tier.assassinsInheritVaultModifiers()) {
            inheritVaultModifiers(vault, assassin);
        }
        if (tier.assassinsGainInfernalModifiers()) {
            applyInfernalModifiers(assassin);
        }
    }

    /**
     * Looter 1+. Applies the vault's mob attribute modifiers by hand, which an {@code addFreshEntity} spawn never
     * receives; {@code applyToEntity} is UUID-keyed and cannot double-apply.
     */
    private static void inheritVaultModifiers(Vault vault, LivingEntity assassin) {
        if (!vault.has(Vault.MODIFIERS)) {
            WoldsVaults.LOGGER.warn("Greed assassin cannot inherit vault modifiers: the vault carries no modifier stack.");
            return;
        }
        Modifiers modifiers = vault.get(Vault.MODIFIERS);
        int applied = 0;
        for (Modifiers.Entry entry : modifiers.getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            ModifierContext context = modifiers.getContext(entry);
            if (modifier instanceof MobAttributeModifier mob) {
                mob.applyToEntity(assassin, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable) {
                settable.applyToEntity(assassin, context.getUUID(), context);
                applied++;
            }
        }
        assassin.setHealth(assassin.getMaxHealth());
        WoldsVaults.LOGGER.debug("Greed assassin inherited {} vault mob attribute modifiers.", applied);
    }

    /** Champion 1+. Draws an exact {@value #INFERNAL_MODIFIER_COUNT} modifiers from the brutal-boss infernal pool. */
    private static void applyInfernalModifiers(LivingEntity assassin) {
        try {
            InfernalMobsCore.instance().addEntityModifiersByString(assassin,
                    BrutalBossesRegistry.getRandomMobModifiers(INFERNAL_MODIFIER_COUNT, false));
        } catch (Throwable throwable) {
            if (!infernalFailureLogged) {
                infernalFailureLogged = true;
                WoldsVaults.LOGGER.error("Greed assassins could not be given infernal modifiers; the Champion tier behaviour is inactive for this session.", throwable);
            }
        }
    }

    /** Hunter 1+. One random debuff per landed melee hit, five seconds each. */
    public static void applyRandomNegativeEffect(Player target, Random random) {
        int index = random.nextInt(NEGATIVE_EFFECTS.length);
        target.addEffect(new MobEffectInstance(NEGATIVE_EFFECTS[index], NEGATIVE_EFFECT_TICKS,
                NEGATIVE_EFFECT_AMPLIFIERS[index], false, true));
    }

    /** Hunter 3+. An eight block pulse granting nearby vault mobs Strength I and Speed I for six seconds. */
    public static void pulseBuffingAura(ServerLevel level, LivingEntity assassin) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                assassin.getBoundingBox().inflate(AURA_RADIUS), GreedAssassins::isAuraTarget);
        for (LivingEntity ally : nearby) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, AURA_BUFF_TICKS, 0, false, false));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, AURA_BUFF_TICKS, 0, false, false));
        }
    }
}

package xyz.iwolfking.woldsvaults.medallions.assassins;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedModifierInheritance;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;

import java.util.List;
import java.util.Random;

/**
 * What greed assassins do beyond fighting: the rank-gated additions at spawn, the debuff and true-damage follow-up on a
 * landed hit, and the aura pulse. The behaviours read the rank stamped on the assassin; their magnitudes come from the
 * medallion table's assassins block.
 */
public final class GreedAssassinBehaviors {
    private GreedAssassinBehaviors() {
    }

    /** Applies the rank-gated additions at spawn; {@code EntityScaler.scale} has already run by this point. */
    public static void applyOnSpawn(Vault vault, GreedMedallionTier tier, LivingEntity assassin) {
        if (tier.assassinsInheritVaultModifiers()) {
            int applied = GreedModifierInheritance.apply(vault, assassin);
            assassin.setHealth(assassin.getMaxHealth());
            WoldLogger.debug("Greed assassin inherited {} vault mob attribute modifiers.", applied);
        }
        if (tier.assassinsGainInfernalModifiers()) {
            applyInfernalModifiers(assassin);
        }
    }

    private static void applyInfernalModifiers(LivingEntity assassin) {
        GreedMedallionsConfig.Assassins settings = GreedMedallionTier.assassinSettings();
        if (settings == null || settings.infernalModifierCount <= 0) {
            return;
        }
        try {
            InfernalMobsCore.instance().addEntityModifiersByString(assassin,
                    BrutalBossesRegistry.getRandomMobModifiers(settings.infernalModifierCount, false));
        } catch (Throwable throwable) {
            WoldLogger.errorOnce("greed-assassin-infernal", "Greed assassins could not be given infernal modifiers; "
                    + "that rank behaviour is inactive.", throwable);
        }
    }

    /**
     * The flat true-damage follow-up and the gated debuff on a landed melee hit; only direct hits count, and the
     * true-damage flag stops the follow-up landing back here.
     */
    public static void onHit(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || WoldActiveFlags.IS_TRUE_DAMAGE.isSet()) {
            return;
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker == null || source.getDirectEntity() != attacker || !GreedAssassins.isAssassin(attacker)) {
            return;
        }
        if (attacker instanceof LivingEntity assassin) {
            VaultTrueDamage.deal(assassin, player, VaultChampion.config().getTrueDamage().assassin);
        }
        GreedAssassins.getTier(attacker)
                .filter(GreedMedallionTier::assassinsInflictNegativeEffects)
                .ifPresent(tier -> applyRandomNegativeEffect(player, player.level.random));
    }

    private static void applyRandomNegativeEffect(Player target, Random random) {
        GreedMedallionsConfig.Assassins settings = GreedMedallionTier.assassinSettings();
        if (settings == null) {
            return;
        }
        GreedMedallionsConfig.Effect entry = settings.negativeEffects.get(random.nextInt(settings.negativeEffects.size()));
        MobEffect effect = resolve(entry);
        if (effect != null) {
            target.addEffect(new MobEffectInstance(effect, settings.negativeEffectTicks, entry.amplifier, false, true));
        }
    }

    /**
     * Pulses the aura of every live assassin whose rank grants one, once every {@code auraPulseTicks}; the sweep also
     * prunes assassins that no longer resolve.
     */
    public static void pulseAuras(MinecraftServer server) {
        GreedMedallionsConfig.Assassins settings = GreedMedallionTier.assassinSettings();
        if (settings == null || server.getTickCount() % settings.auraPulseTicks != 0) {
            return;
        }
        GreedAssassinRegistry.forEach(server, (level, assassin) ->
                GreedAssassins.getTier(assassin)
                        .filter(GreedMedallionTier::assassinsGainBuffingAuras)
                        .ifPresent(tier -> pulseBuffingAura(level, assassin, settings)));
    }

    private static void pulseBuffingAura(ServerLevel level, LivingEntity assassin, GreedMedallionsConfig.Assassins settings) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                assassin.getBoundingBox().inflate(settings.auraRadius), GreedAssassins::isAuraTarget);
        for (GreedMedallionsConfig.Effect entry : settings.auraEffects) {
            MobEffect effect = resolve(entry);
            if (effect == null) {
                continue;
            }
            for (LivingEntity ally : nearby) {
                ally.addEffect(new MobEffectInstance(effect, settings.auraBuffTicks, entry.amplifier, false, false));
            }
        }
    }

    private static MobEffect resolve(GreedMedallionsConfig.Effect entry) {
        ResourceLocation id = ResourceLocation.tryParse(entry.effect);
        MobEffect effect = id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            WoldLogger.errorOnce("greed-assassin-effect-" + entry.effect, "The greed medallion table names effect '{}', "
                    + "which is not registered; greed assassins skip it.", entry.effect);
        }
        return effect;
    }
}

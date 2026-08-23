package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.init.ModEffects;
import iskallia.vault.skill.ability.effect.RampageAbility;
import iskallia.vault.skill.ability.effect.RampageBerserkerAbility;
import iskallia.vault.skill.ability.effect.RampageBloodlustAbility;
import iskallia.vault.skill.ability.effect.RampageInstinctAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractRampageAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;

/**
 * The Rampage damage bonus a player is actually receiving right now, as a fraction.
 *
 * <p>This exists because the number moved. In the_vault 3.21.5 Rampage registered an
 * {@code ADDITIVE_MULTIPLY} entry in {@code PlayerDamageHelper}, so anything wanting to read it
 * could ask that registry. In 3.21.6 that registration is gone: {@code RampageAbility} multiplies
 * {@code LivingHurtEvent} directly at {@code EventPriority.LOW}, and the registry now holds only
 * Aspect of Berserk, the Berserker archetype and Barbarian's rage. Reading the registry to find
 * "the Rampage bonus" therefore returns entirely the wrong number.
 *
 * <p>The lookup mirrors {@code RampageAbility#applyActiveRampageDamage}: one pass per Rampage
 * effect the base mod checks, taking the first unlocked ability of the matching class. The four
 * are combined multiplicatively because that is how the base mod applies them - one
 * {@code setAmount(amount * (1 + increase))} per active effect - so the fraction returned here is
 * exactly what a qualifying melee hit is multiplied by, whether one specialization is live or
 * (unusually) several.
 */
public final class RampageAccess {
    private RampageAccess() {
    }

    private record Branch(MobEffect effect, Class<? extends AbstractRampageAbility> ability) {
    }

    private static Branch[] branches() {
        return new Branch[]{
                new Branch(ModEffects.RAMPAGE, RampageAbility.class),
                new Branch(ModEffects.RAMPAGE_BLOODLUST, RampageBloodlustAbility.class),
                new Branch(ModEffects.RAMPAGE_BERSERKER, RampageBerserkerAbility.class),
                new Branch(ModEffects.RAMPAGE_INSTINCT, RampageInstinctAbility.class)
        };
    }

    public static boolean hasAnyRampageEffect(ServerPlayer player) {
        for (Branch branch : branches()) {
            if (player.hasEffect(branch.effect())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The combined Rampage damage increase in effect for this player, Ultra Rampaging included.
     *
     * <p>Ultra Rampaging needs no special handling here: it rewrites {@code getDamageIncrease}
     * itself, so calling that method returns the boosted value automatically. Returns zero when no
     * Rampage effect is active, which is the correct reading - Rampage that is toggled off
     * multiplies nothing, however much Fury the player is holding.
     */
    public static float effectiveDamageIncrease(ServerPlayer player) {
        if (!(player.level instanceof ServerLevel level)) {
            return 0.0F;
        }
        AbilityTree abilities = PlayerAbilitiesData.get(level).getAbilities(player);
        float product = 1.0F;
        for (Branch branch : branches()) {
            if (!player.hasEffect(branch.effect())) {
                continue;
            }
            for (AbstractRampageAbility ability : abilities.getAll(branch.ability(), Skill::isUnlocked)) {
                product *= 1.0F + ability.getDamageIncrease(player);
                break;
            }
        }
        return product - 1.0F;
    }
}

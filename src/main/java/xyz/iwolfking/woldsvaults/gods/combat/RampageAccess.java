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
 * The Rampage damage bonus a player is actually receiving right now, as a fraction. The four
 * Rampage effects combine multiplicatively, matching how the base mod applies them.
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

    /** The combined Rampage increase, Ultra Rampaging included; zero when no Rampage effect is active. */
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

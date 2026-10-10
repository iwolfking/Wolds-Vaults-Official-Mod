package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.IStoredAbilityTier;

import java.util.Optional;

public class AbilityHelper {

    public static int getAbilityLevel(ServerPlayer player, String abilityId) {
        AbilityTree tree = PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);
        if (tree == null || abilityId == null) {
            return 0;
        }

        Optional<Skill> targetSkillOpt = tree.getForId(abilityId);
        if (targetSkillOpt.isEmpty()) {
            return 0;
        }

        Skill targetSkill = targetSkillOpt.get();

        if (targetSkill instanceof TieredSkill tieredSkill) {
            return tieredSkill.getActualTier();
        }

        if (targetSkill instanceof Ability) {
            Skill parent = targetSkill.getParent();
            if (parent instanceof TieredSkill tieredSkill) {
                return tieredSkill.getActualTier();
            }
        }

        return 0;
    }

    public static Optional<InstantAbility> getAbilityRefFromConfig(String abilityId, int level) {
        Optional<Skill> skillOpt = ModConfigs.ABILITIES.getAbilityById(abilityId);
        if(skillOpt.isPresent()) {
            Skill skill = skillOpt.get();
            if (skill instanceof TieredSkill tieredSkill) {
                if (tieredSkill.getChild(level) instanceof InstantAbility ability) {
                    WoldsVaults.LOGGER.info(String.valueOf(level));
                    if(ability instanceof IStoredAbilityTier storedAbilityTier) {
                        WoldsVaults.LOGGER.info("Ability is IStoredAbilityTier");
                        storedAbilityTier.setTierLevel(level);
                    }
                    return Optional.of(ability);
                }
            }
        }

        return Optional.empty();
    }

    public static float getScaledByLevelDamageFalloff(float originalFalloff, ServerPlayer player, String abilityId) {
        float maxFallOffPerLevel = 0.2F + (AbilityHelper.getAbilityLevel(player, abilityId) * 0.025F);
        return Math.max(originalFalloff, maxFallOffPerLevel);
    }
}

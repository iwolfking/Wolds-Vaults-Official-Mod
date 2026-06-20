package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
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
}

package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.server.level.ServerPlayer;
import java.util.Optional;

public class TalentHelper {

    public static <T extends Skill> Optional<T> getTalent(ServerPlayer player, Class <T> type) {
        TalentTree tree = PlayerTalentsData.get(player.getLevel()).getTalents(player);
        if (tree == null || type == null) {
            return Optional.empty();
        }

        return tree.getAll(type, t -> t.isUnlocked() && type.isInstance(t)).stream().findFirst();
    }
}

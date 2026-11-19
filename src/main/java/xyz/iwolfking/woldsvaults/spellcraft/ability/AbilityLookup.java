package xyz.iwolfking.woldsvaults.spellcraft.ability;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public final class AbilityLookup {
    private AbilityLookup() {
    }

    public static AbilityTree tree(ServerPlayer player) {
        return PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);
    }

    /** The current tier object of an ability group, or null when it isn't learned. */
    @Nullable
    public static Ability ability(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() instanceof TieredSkill tiered
                && tiered.isUnlocked() && tiered.getChild() instanceof Ability ability) {
            return ability;
        }
        return null;
    }

    /** Maps an ability group to the tree's current specialization id, or null when the tree has no such group. */
    public static Function<String, String> specOf(AbilityTree tree) {
        return group -> {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() != null) {
                return specialized.getSpecialization().getId();
            }
            return null;
        };
    }

    /** Whether {@code skill} is the specialization {@code spec} itself or that specialization's tier ability. */
    public static boolean belongsTo(@Nullable Skill skill, @Nullable String spec) {
        return skill != null && spec != null && (spec.equals(skill.getId()) || spec.equals(specId(skill)));
    }

    @Nullable
    public static String specId(Skill ability) {
        Skill parent = ability.getParent();
        return parent == null ? null : parent.getId();
    }

    @Nullable
    public static String groupOf(Ability ability) {
        Skill parent = ability.getParent();
        Skill group = parent == null ? null : parent.getParent();
        return group == null ? null : group.getId();
    }

    public static String groupName(String group) {
        return group.replace('_', ' ');
    }

    public static Optional<LearnableSkill> child(AbilityTree tree, String spec) {
        return tree.getForId(spec).filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast).map(TieredSkill::getChild);
    }

    public static Optional<LearnableSkill> unlockedChild(AbilityTree tree, String spec) {
        return tree.getForId(spec).filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast).filter(TieredSkill::isUnlocked)
                .map(TieredSkill::getChild);
    }
}

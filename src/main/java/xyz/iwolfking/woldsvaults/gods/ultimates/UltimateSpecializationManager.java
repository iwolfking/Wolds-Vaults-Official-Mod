package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;

import java.util.Optional;

/**
 * Keeps the single {@code Stirrings of Power} {@link SpecializedSkill} pointed at the right ultimate: four
 * god specializations plus a dormant fifth, chosen from the equipped charm and god level rather than by
 * the player. The node is learned automatically and costs no skill points.
 */
public final class UltimateSpecializationManager {
    private UltimateSpecializationManager() {
    }

    public static String resolveSpecialization(ServerPlayer player) {
        if (player.getServer() == null) {
            return UltimateIds.DORMANT;
        }
        Optional<VaultGod> god = ActiveGodResolver.getActiveGod(player);
        if (god.isEmpty()) {
            return UltimateIds.DORMANT;
        }
        int level = GodAlignmentData.get(player.getServer()).getLevel(player.getUUID(), god.get());
        return GodLevels.hasUltimate(level) ? UltimateIds.specializationFor(god.get()) : UltimateIds.DORMANT;
    }

    /** Re-points and, if needed, learns the ability; does nothing once the tree already agrees. */
    public static void refresh(ServerPlayer player) {
        if (player.getServer() == null) {
            return;
        }
        PlayerAbilitiesData data = PlayerAbilitiesData.get(player.getLevel());
        AbilityTree tree = data.getAbilities(player);
        Skill node = tree.getForId(UltimateIds.STIRRINGS_OF_POWER).orElse(null);
        if (!(node instanceof SpecializedSkill specialized)) {
            return;
        }
        String target = resolveSpecialization(player);
        SkillContext context = SkillContext.of(player);
        boolean changed = false;
        Skill current = specialized.getSpecialization();
        if (current == null || !target.equals(current.getId())) {
            specialized.specialize(target, context);
            changed = true;
        }
        if (!specialized.isUnlocked() && specialized.canLearn(context)) {
            specialized.learn(context);
            changed = true;
        }
        if (changed) {
            data.setDirty();
            tree.sync(context);
        }
    }
}

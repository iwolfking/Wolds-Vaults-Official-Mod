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
 * Keeps the single {@code Stirrings of Power} ability pointed at the right ultimate.
 *
 * <p>The framework already has the primitive this needs: an ability node is a
 * {@link SpecializedSkill} holding alternative specializations behind an index, and both the HUD
 * icon and the displayed name are read from whichever specialization is selected. So the four god
 * ultimates are not four abilities, they are four specializations of one — plus a fifth, dormant
 * one for players who are owed no ultimate. Choosing for the player is the only unusual part: the
 * base mod expects the player to pick, we pick from their equipped charm and their god level.
 *
 * <p>The node is learned automatically. It costs no skill points by design, so no ability screen is
 * needed to reach it and the deferred god-tree UI is not on the critical path for casting.
 */
public final class UltimateSpecializationManager {
    private UltimateSpecializationManager() {
    }

    /** The specialization this player should currently be on. */
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

    /**
     * Re-points and, if needed, learns the ability. Cheap enough to call on a timer: it is a map
     * lookup plus an id walk, and it does nothing at all once the tree already agrees.
     */
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

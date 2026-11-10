package xyz.iwolfking.woldsvaults.talent.dual_wield;

import iskallia.vault.skill.base.LearnableSkill;
import net.bettercombat.logic.PlayerAttackHelper;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public abstract class DualWieldTalent extends LearnableSkill {

    protected DualWieldTalent(int unlockLevel, int learnPointCost, int regretPointCost) {
        super(unlockLevel, learnPointCost, regretPointCost);
    }

    protected DualWieldTalent() {
    }

    public static boolean isDualWielding(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        if (!WoldsVaults.BETTER_COMBAT_PRESENT) {
            return !player.getMainHandItem().isEmpty()
                    && !player.getOffhandItem().isEmpty()
                    && player.getOffhandItem().isDamageableItem() && player.getMainHandItem().getItem().equals(player.getOffhandItem().getItem());
        }

        try {
            return PlayerAttackHelper.isDualWielding(player);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
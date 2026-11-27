package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Massive Chests and Expert Looter: "+N maximum item stacks", additive. The bonus raises the
 * generator's roll cap only; the table's base roll and Item Quantity scaling are unchanged.
 */
public final class TenosChestRolls {
    private TenosChestRolls() {
    }

    public static int bonusRolls(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int bonus = 0;
        if (TenosNodes.isActive(serverPlayer, TenosNodes.MASSIVE_CHESTS)) {
            bonus += TenosNodeHandlers.params(TenosNodes.MASSIVE_CHESTS,
                    TenosNodeHandlers.MassiveChestsParams.class).rolls();
        }
        int looterPoints = TenosNodes.points(serverPlayer, TenosNodes.EXPERT_LOOTER);
        if (looterPoints > 0) {
            bonus += TenosNodeHandlers.params(TenosNodes.EXPERT_LOOTER,
                    TenosNodeHandlers.ExpertLooterParams.class).rolls() * looterPoints;
        }
        return bonus;
    }
}

package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Velara's Pious Devotion node as a piety source: ten piety per invested point toward Velara,
 * carried at a quarter like every other foreign-tree value when Velara is not the active god.
 */
public final class VelaraPiety implements PietyBonusSource {
    @Override
    public int getBonusPiety(Player player, VaultGod god) {
        if (god != VaultGod.VELARA || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = VelaraNodeState.investedPoints(serverPlayer, VelaraNode.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, VaultGod.VELARA) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(VelaraValues.piousDevotionPietyPerPoint() * points * scale);
    }
}

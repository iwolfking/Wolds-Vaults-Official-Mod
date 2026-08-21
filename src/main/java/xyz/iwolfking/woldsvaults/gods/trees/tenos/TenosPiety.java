package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Tenos's Pious Devotion node as a piety source: ten piety per invested point toward Tenos,
 * carried at a quarter like every other foreign-tree value when Tenos is not the active god.
 */
public final class TenosPiety implements PietyBonusSource {
    public static final float PIETY_PER_POINT = 10.0F;

    @Override
    public int getBonusPiety(Player player, VaultGod god) {
        if (god != TenosNodes.GOD || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = TenosNodes.ledgerPoints(serverPlayer, TenosNodes.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, TenosNodes.GOD) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(PIETY_PER_POINT * points * scale);
    }
}

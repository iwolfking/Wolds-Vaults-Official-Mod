package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Wendarr's Pious Devotion node as a piety source: ten piety per invested point toward Wendarr,
 * carried at a quarter like every other foreign-tree value when Wendarr is not the active god.
 */
public final class WendarrPiety implements PietyBonusSource {
    public static final float PIETY_PER_POINT = 10.0F;

    @Override
    public int getBonusPiety(Player player, VaultGod god) {
        if (god != WendarrNodes.GOD || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = WendarrNodes.ledgerPoints(serverPlayer, WendarrNodes.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, WendarrNodes.GOD) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(PIETY_PER_POINT * points * scale);
    }
}

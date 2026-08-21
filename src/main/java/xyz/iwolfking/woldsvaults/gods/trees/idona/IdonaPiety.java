package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.handlers.PietyHandler;

/**
 * Idona's Pious Devotion node as a piety source: the effect's configured piety per invested point
 * toward Idona, carried at a quarter like every other foreign-tree value when Idona is not the
 * active god, so the value of investing in the tree does not vanish the moment the charm changes.
 */
public final class IdonaPiety implements PietyBonusSource {
    @Override
    public int getBonusPiety(Player player, VaultGod god) {
        if (god != IdonaNodes.GOD || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = IdonaNodes.investedPoints(serverPlayer, IdonaNodes.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        PietyHandler handler = GodNodeRegistry.handler(IdonaNodes.PIOUS_DEVOTION, PietyHandler.class);
        if (handler == null) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, IdonaNodes.GOD) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(handler.perPoint() * points * scale);
    }
}

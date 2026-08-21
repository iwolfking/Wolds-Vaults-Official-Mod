package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.handlers.PietyHandler;

/**
 * Tenos's Pious Devotion node as a piety source: the effect's configured piety per invested point
 * toward Tenos, carried at a quarter like every other foreign-tree value when Tenos is not the
 * active god, so the value of investing in the tree does not vanish the moment the charm changes.
 */
public final class TenosPiety implements PietyBonusSource {
    @Override
    public int getBonusPiety(Player player, VaultGod god) {
        if (god != TenosNodes.GOD || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = TenosNodes.investedPoints(serverPlayer, TenosNodes.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        PietyHandler handler = GodNodeRegistry.handler(TenosNodes.PIOUS_DEVOTION, PietyHandler.class);
        if (handler == null) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, TenosNodes.GOD) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(handler.perPoint() * points * scale);
    }
}

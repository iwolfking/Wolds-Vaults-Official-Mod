package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.handlers.PietyHandler;

/**
 * A god tree's Pious Devotion node as a piety source: the effect's configured piety per invested
 * point toward that god, carried at a quarter like every other foreign-tree value when the god is
 * not the active one, so the value of investing does not vanish the moment the charm changes.
 *
 * <p>One implementation serves all four trees. Each tree previously shipped its own copy, and the
 * four differed only in which god they answered for - which is the parameter.
 */
public record GodPietySource(VaultGod god, String effectId) implements PietyBonusSource {

    @Override
    public int getBonusPiety(Player player, VaultGod queried) {
        if (queried != this.god || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = GodVaultUtil.investedPoints(serverPlayer, this.god, this.effectId);
        if (points <= 0) {
            return 0;
        }
        PietyHandler handler = GodNodeRegistry.handler(this.effectId, PietyHandler.class);
        if (handler == null) {
            return 0;
        }
        float scale = ActiveGodResolver.isActive(player, this.god) ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
        return Math.round(handler.perPoint() * points * scale);
    }
}

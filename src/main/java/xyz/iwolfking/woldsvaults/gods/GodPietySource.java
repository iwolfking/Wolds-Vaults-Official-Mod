package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.handlers.PietyHandler;

/** A Pious Devotion node as a piety source: configured piety per point, at {@link GodNodeCache#treeScale}. */
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
        float scale = GodNodeCache.treeScale(serverPlayer, this.god);
        if (scale <= 0.0F) {
            return 0;
        }
        return Math.round(handler.perPoint() * points * scale);
    }
}

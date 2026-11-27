package xyz.iwolfking.woldsvaults.spellcraft.buff;

import iskallia.vault.mana.ManaAction;
import iskallia.vault.mana.ManaPlayer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Refunds Vault mana. VH has no mana-cost stat event, so a toggle discount is refunded as a share of the mana the
 * toggle actually took.
 */
public final class ManaRefunds {
    private ManaRefunds() {
    }

    public static void refund(ServerPlayer player, float amount) {
        if (amount > 0) {
            ((ManaPlayer) player).increaseMana(ManaAction.PLAYER_ACTION, amount);
        }
    }
}

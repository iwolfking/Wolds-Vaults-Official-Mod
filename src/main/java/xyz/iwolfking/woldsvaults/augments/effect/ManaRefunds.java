package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.mana.ManaAction;
import iskallia.vault.mana.ManaPlayer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

/**
 * Discount and Shotgun copy refunds. VH has no mana-cost stat event (costs come from gear, {@code ManaCostHelper}),
 * so the refund is a share of the Vault mana a cast actually took, measured around the cast.
 */
public final class ManaRefunds {
    private ManaRefunds() {
    }

    public static <T> T withRefund(ServerPlayer player, float fraction, Supplier<T> action) {
        if (fraction <= 0) {
            return action.get();
        }
        float before = ((ManaPlayer) player).getMana();
        T result = action.get();
        refund(player, (before - ((ManaPlayer) player).getMana()) * Math.min(1.0F, fraction));
        return result;
    }

    public static void refund(ServerPlayer player, float amount) {
        if (amount > 0) {
            ((ManaPlayer) player).increaseMana(ManaAction.PLAYER_ACTION, amount);
        }
    }
}

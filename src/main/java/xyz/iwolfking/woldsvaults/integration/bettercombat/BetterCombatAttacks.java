package xyz.iwolfking.woldsvaults.integration.bettercombat;

import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public final class BetterCombatAttacks {
    private static final String MOD_ID = "bettercombat";

    private BetterCombatAttacks() {
    }

    /** The hand of the attack the player is making right now: the offhand during a Better Combat offhand swing, otherwise the main hand. */
    public static InteractionHand attackingHand(Player player) {
        if (ModList.get().isLoaded(MOD_ID) && isOffHandAttack(player)) {
            return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    private static boolean isOffHandAttack(Player player) {
        if (!(player instanceof EntityPlayer_BetterCombat betterCombatPlayer)) {
            return false;
        }
        AttackHand hand = betterCombatPlayer.getCurrentAttack();
        return hand != null && hand.isOffHand();
    }
}

package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Massive Chests (r105) and Expert Looter (r107): "chests roll with +N maximum item stacks".
 *
 * <p><b>What "+N stacks" was made to mean.</b> Taken literally the phrase raises a cap, and the
 * scoping pass showed every cap in the chest path is close to a no-op: the binding constant is the
 * 27-slot fill cap, {@code fillLoot} only spreads the loot that already exists across whatever
 * slots there are, and the 54-roll cap is almost never reached at real item quantity. Raising
 * either creates no items, while Massive Chests pays a real, unconditional halving of item rarity.
 * So the implemented mechanic is the third reading from the scoping doc: <b>+N extra loot rolls</b>
 * before the roll cap, +20 for Massive Chests and +4 for Expert Looter, additive with each other.
 * The 27-slot fill cap is lifted for those chests as well, otherwise the extra stacks would be
 * generated and then silently discarded when the slot list ran out.
 */
public final class TenosChestRolls {
    private TenosChestRolls() {
    }

    /** Extra loot rolls this player's chests generate, before the generator's roll cap. */
    public static int bonusRolls(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int bonus = 0;
        if (TenosNodes.isActive(serverPlayer, TenosNodes.MASSIVE_CHESTS)) {
            bonus += TenosNodeHandlers.params(TenosNodes.MASSIVE_CHESTS,
                    TenosNodeHandlers.MassiveChestsParams.class).rolls();
        }
        int looterPoints = TenosNodes.points(serverPlayer, TenosNodes.EXPERT_LOOTER);
        if (looterPoints > 0) {
            bonus += TenosNodeHandlers.params(TenosNodes.EXPERT_LOOTER,
                    TenosNodeHandlers.ExpertLooterParams.class).rolls() * looterPoints;
        }
        return bonus;
    }
}

package xyz.iwolfking.woldsvaults.milestones.container;

import iskallia.vault.container.spi.AbstractElementContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModContainers;

/** Slotless, payloadless menu behind the greed tab; the screen renders from {@code ClientMilestoneData}. */
public class GreedMilestonesContainer extends AbstractElementContainer {
    public GreedMilestonesContainer(int windowId, Player player) {
        super(ModContainers.GREED_MILESTONES_CONTAINER, windowId, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

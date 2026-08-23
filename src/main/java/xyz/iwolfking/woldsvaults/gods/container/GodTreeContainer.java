package xyz.iwolfking.woldsvaults.gods.container;

import iskallia.vault.container.spi.AbstractElementContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModContainers;

/** Slotless menu behind the gods tab of the player menu; it carries no payload. */
public class GodTreeContainer extends AbstractElementContainer {
    public GodTreeContainer(int windowId, Player player) {
        super(ModContainers.GOD_TREE_CONTAINER, windowId, player);
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

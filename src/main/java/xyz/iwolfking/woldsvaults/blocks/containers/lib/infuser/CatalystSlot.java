package xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import xyz.iwolfking.woldsvaults.data.recipes.CachedInfuserRecipeData;

public class CatalystSlot extends SlotItemHandler {
    private final BaseItemStackHandler inventory;
    private final int index;

    public CatalystSlot(BaseItemStackHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.inventory = inventory;
        this.index = index;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        System.out.println(CachedInfuserRecipeData.getCatalysts().contains(stack.getItem()));
        return CachedInfuserRecipeData.getCatalysts().contains(stack.getItem());
    }

    @Override
    public ItemStack remove(int amount) {
        return this.inventory.extractItemSuper(this.index, amount, false);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 64;
    }
}

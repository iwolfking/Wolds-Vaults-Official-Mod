package xyz.iwolfking.woldsvaults.api.core.competition.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardBundle {
    private final List<ItemStack> items;

    public RewardBundle(List<ItemStack> items) {
        this.items = items;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean remove(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (ItemStack.isSame(item, stack)) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }


    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack stack : items) {
            list.add(stack.save(new CompoundTag()));
        }
        tag.put("Items", list);
        return tag;
    }

    public static RewardBundle load(CompoundTag tag) {
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            items.add(ItemStack.of(list.getCompound(i)));
        }
        return new RewardBundle(items);
    }
}

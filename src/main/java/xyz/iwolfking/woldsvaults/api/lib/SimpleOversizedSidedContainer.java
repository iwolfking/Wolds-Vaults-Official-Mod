package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.container.base.SimpleSidedContainer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public abstract class SimpleOversizedSidedContainer extends SimpleSidedContainer {
    private static final int MAX_SERIALIZED_ITEM_COUNT = 64;

    private final Map<Direction, Set<Integer>> cachedSidedSlots = new EnumMap<>(Direction.class);

    public SimpleOversizedSidedContainer(int size) {
        super(size);
        this.cacheSlots();
    }


    private void cacheSlots() {
        IntStream.range(0, this.getContainerSize()).forEach(
                slot -> this.getAccessibleSlots(slot)
                        .forEach(dir ->
                                (this.cachedSidedSlots.computeIfAbsent(dir, side -> new HashSet<>())).add(slot)
                        )
        );
    }

    public abstract List<Direction> getAccessibleSlots(int var1);

    @Override
    public int[] getSlotsForFace(Direction side) {
        return (Optional.ofNullable(this.cachedSidedSlots.get(side)).stream().flatMap(Collection::stream)).mapToInt(x -> x).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return (this.cachedSidedSlots.getOrDefault(side, Collections.emptySet())).contains(slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return (this.cachedSidedSlots.getOrDefault(side, Collections.emptySet())).contains(slot);
    }

    @Override
    public int getMaxStackSize() {
        return 2147483582;
    }

    public ListTag createOversizedTag() {
        ListTag list = new ListTag();

        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            ItemStack stack = this.getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putInt("slot", slot);
                entryTag.put("stack", serializeOversizedStack(stack));
                list.add(entryTag);
            }
        }

        return list;
    }

    public void fromOversizedTag(ListTag list) {
        this.clearContent();

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            if (!entryTag.contains("stack", Tag.TAG_COMPOUND)) {
                continue;
            }

            int slot = entryTag.contains("slot", Tag.TAG_INT) ? entryTag.getInt("slot") : i;
            if (slot < 0 || slot >= this.getContainerSize()) {
                continue;
            }

            ItemStack stack = deserializeOversizedStack(entryTag.getCompound("stack"));
            if (!stack.isEmpty()) {
                this.setItem(slot, stack);
            }
        }
    }

    private static CompoundTag serializeOversizedStack(ItemStack stack) {
        int amount = stack.getCount();
        ItemStack serializable = stack.copy();
        serializable.setCount(Math.min(amount, MAX_SERIALIZED_ITEM_COUNT));

        CompoundTag tag = new CompoundTag();
        tag.put("stack", serializable.serializeNBT());
        tag.putInt("amount", amount);
        return tag;
    }

    private static ItemStack deserializeOversizedStack(CompoundTag tag) {
        if (tag.contains("amount", Tag.TAG_INT) && tag.contains("stack", Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.of(tag.getCompound("stack"));
            int amount = tag.getInt("amount");
            if (stack.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }

            stack.setCount(amount);
            return stack;
        }

        return ItemStack.of(tag);
    }
}
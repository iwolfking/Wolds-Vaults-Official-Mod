package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.container.base.SimpleSidedContainer;
import net.minecraft.core.Direction;
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
}

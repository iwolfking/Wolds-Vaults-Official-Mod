package xyz.iwolfking.woldsvaults.blocks.containers.lib;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

import java.util.function.IntSupplier;

/**
 * Menu data that carries full ints. Vanilla sends each data slot to the client as a short, so every value here
 * occupies two slots (low and high 16 bits) and is put back together with {@link #read}.
 */
public final class WideContainerData implements ContainerData {
    private final IntSupplier[] values;

    public WideContainerData(IntSupplier... values) {
        this.values = values;
    }

    /** Client-side storage for {@code count} wide values. */
    public static ContainerData client(int count) {
        return new SimpleContainerData(count * 2);
    }

    /** The wide value {@code index} out of data written by a {@link WideContainerData}. */
    public static int read(ContainerData data, int index) {
        return (data.get(index * 2) & 0xFFFF) | ((data.get(index * 2 + 1) & 0xFFFF) << 16);
    }

    @Override
    public int get(int index) {
        int value = this.values[index / 2].getAsInt();
        return (index % 2 == 0 ? value : value >>> 16) & 0xFFFF;
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return this.values.length * 2;
    }
}

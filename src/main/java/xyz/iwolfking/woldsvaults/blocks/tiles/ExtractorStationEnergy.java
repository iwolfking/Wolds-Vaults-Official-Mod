package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

/**
 * The FE buffer of an extractor station. Pipes only fill it, at up to the input rate; only the station's own job
 * draws from it. Capacity and input rate are read from the station's table row on every call, so a table reload
 * applies at once.
 */
public class ExtractorStationEnergy implements IEnergyStorage {
    private final IntSupplier capacity;
    private final IntSupplier input;
    private final Runnable onChanged;
    private int stored;

    public ExtractorStationEnergy(IntSupplier capacity, IntSupplier input, Runnable onChanged) {
        this.capacity = capacity;
        this.input = input;
        this.onChanged = onChanged;
    }

    /** Draws {@code amount} FE for the station's job; false, with nothing drawn, when the buffer is short. */
    public boolean draw(int amount) {
        if (this.stored < amount) {
            return false;
        }
        this.stored -= amount;
        return true;
    }

    /** Restores a saved amount. */
    public void load(int amount) {
        this.stored = Math.max(0, amount);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int room = Math.max(0, this.getMaxEnergyStored() - this.stored);
        int received = Math.max(0, Math.min(room, Math.min(maxReceive, this.input.getAsInt())));
        if (received > 0 && !simulate) {
            this.stored += received;
            this.onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.stored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Math.max(0, this.capacity.getAsInt());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}

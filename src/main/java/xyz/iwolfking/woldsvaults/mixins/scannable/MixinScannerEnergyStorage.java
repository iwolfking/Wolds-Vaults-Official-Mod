package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.client.ScanManager;
import li.cil.scannable.common.config.CommonConfig;
import li.cil.scannable.common.energy.ScannerEnergyStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ScannerEnergyStorage.class, remap = false)
public class MixinScannerEnergyStorage extends EnergyStorage {
    @Shadow
    @Final
    private ItemStack container;

    public MixinScannerEnergyStorage(int capacity) {
        super(capacity);
    }

    /**
     * @author iwolfking
     * @reason Always extract energy
     */
    @Overwrite
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && energyExtracted != 0) {
            this.container.addTagElement("energy", this.serializeNBT());
        }

        return energyExtracted;

    }

    /**
     * @author iwolfking
     * @reason Always receive energy
     */
    @Overwrite
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && energyReceived != 0) {
            this.container.addTagElement("energy", this.serializeNBT());
        }

        return energyReceived;
    }
}

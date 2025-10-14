package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.common.capabilities.Capabilities;
import li.cil.scannable.common.config.Strings;
import li.cil.scannable.common.energy.ScannerEnergyStorage;
import li.cil.scannable.common.item.ModItem;
import li.cil.scannable.common.item.ScannerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = ScannerItem.class, remap = false)
public abstract class MixinScannableItem extends ModItem {
    @Shadow
    private static boolean collectModules(ItemStack scanner, List<ItemStack> modules) {
        return false;
    }

    /**
     * @author iwolfking
     * @reason Hard code module energy cost
     */
    @Overwrite
    static int getModuleEnergyCost(ItemStack stack) {
        return Capabilities.SCANNER_MODULE_CAPABILITY != null ? (Integer) stack.getCapability(Capabilities.SCANNER_MODULE_CAPABILITY).map((module) -> module.getEnergyCost(stack)).orElse(0) : 0;
    }

    /**
     * @author iwolfking
     * @reason Hard code module energy cost
     */
    @Overwrite
    private static float getRelativeEnergy(ItemStack stack) {
        return (Float) stack.getCapability(CapabilityEnergy.ENERGY).map((storage) -> (float) storage.getEnergyStored() / (float) storage.getMaxEnergyStored()).orElse(0.0F);
    }

    /**
     * @author iwolfking
     * @reason Scanner should always consume energy regardless of config
     */
    @Overwrite
    private static boolean tryConsumeEnergy(Player player, ItemStack scanner, List<ItemStack> modules, boolean simulate) {
        if (player.isCreative()) {
            return true;
        } else {
            LazyOptional<IEnergyStorage> energyStorage = scanner.getCapability(CapabilityEnergy.ENERGY);
            if (!energyStorage.isPresent()) {
                return false;
            } else {
                int totalCostAccumulator = modules.stream().mapToInt(MixinScannableItem::getModuleEnergyCost).sum();

                int extracted = (Integer) energyStorage.map((storage) -> storage.extractEnergy(totalCostAccumulator, simulate)).orElse(0);
                return extracted >= totalCostAccumulator;
            }
        }
    }

    /**
     * @author iwolfking
     * @reason Always show energy bar.
     */
    @Overwrite
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    /**
     * @author iwolfking
     * @reason Always show energy tooltip
     */
    @Overwrite
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ScannerEnergyStorage energyStorage = ScannerEnergyStorage.of(stack);
        tooltip.add(Strings.energyStorage(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()));
    }
}

package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.common.energy.ScannerEnergyStorage;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "scannable")
    }
)
@Mixin(value = ScannerEnergyStorage.class, remap = false)
public class MixinScannerEnergyStorage extends EnergyStorage {
    @Shadow
    @Final
    private ItemStack container;

    public MixinScannerEnergyStorage(int capacity) {
        super(capacity);
    }


    @Redirect(method = {"extractEnergy","receiveEnergy"}, at = @At(value = "FIELD", target = "Lli/cil/scannable/common/config/CommonConfig;useEnergy:Z", opcode = Opcodes.GETSTATIC))
    private static boolean alwaysUseEnergy() {
        return true;
    }
}

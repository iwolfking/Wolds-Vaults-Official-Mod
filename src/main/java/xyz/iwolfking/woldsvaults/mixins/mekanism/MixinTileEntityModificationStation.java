package xyz.iwolfking.woldsvaults.mixins.mekanism;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.VaultArmorItem;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = TileEntityModificationStation.class, remap = false)
public abstract class MixinTileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {
    @Shadow
    public int operatingTicks;
    @Shadow
    public int ticksRequired;
    @Shadow
    private MachineEnergyContainer<TileEntityModificationStation> energyContainer;
    @Shadow
    public InputInventorySlot containerSlot;
    @Shadow
    private InputInventorySlot moduleSlot;

    public MixinTileEntityModificationStation(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Inject(method = "lambda$getInitialInventory$1", at = @At("HEAD"), cancellable = true)
    private static void allowVaultGear(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
       if(stack.getItem() instanceof VaultArmorItem) {
            cir.setReturnValue(true);
       }
   }

   @Inject(method = "onUpdateServer", at = @At(value = "INVOKE", target = "Lmekanism/api/MekanismAPI;getModuleHelper()Lmekanism/api/gear/IModuleHelper;", shift = At.Shift.BEFORE))
    private void addVaultGearHandling(CallbackInfo ci, @Local ItemStack stack, @Local ModuleData<?> moduleData, @Local(name = "operated") LocalBooleanRef operatedRef) {
       if(stack.getItem() instanceof VaultArmorItem) {
           if(moduleData.getItemProvider().getItemStack().getItem().equals(MekanismItems.MODULE_RADIATION_SHIELDING.asItem())) {
               VaultGearData data = VaultGearData.read(stack);
               if(data.isModifiable()) {
                   if(data.hasAttribute(ModGearAttributes.RADIATION_IMMUNITY)) {
                       return;
                   }
                   else {
                       operatedRef.set(true);
                       operatingTicks++;
                       energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
                       if (operatingTicks == ticksRequired) {
                           operatingTicks = 0;
                           ItemStack newGearStack = stack.copy();
                           VaultGearData newData = VaultGearData.read(newGearStack);
                           newData.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<>(ModGearAttributes.RADIATION_IMMUNITY, true));
                           newData.write(newGearStack);
                           containerSlot.setStack(newGearStack);
                           MekanismUtils.logMismatchedStackSize(moduleSlot.shrinkStack(1, Action.EXECUTE), 1);
                       }
                   }
               }
           }
       }
   }
}

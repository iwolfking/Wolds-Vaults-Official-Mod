package xyz.iwolfking.woldsvaults.mixins.mekanism;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.ModModuleToVaultGearModifications;

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
       if(stack.getItem() instanceof VaultGearItem) {
            cir.setReturnValue(true);
       }
   }

   @Inject(method = "onUpdateServer", at = @At(value = "INVOKE", target = "Lmekanism/api/MekanismAPI;getModuleHelper()Lmekanism/api/gear/IModuleHelper;", shift = At.Shift.BEFORE))
    private void addVaultGearHandling(CallbackInfo ci, @Local ItemStack stack, @Local ModuleData<?> moduleData, @Local(name = "operated") LocalBooleanRef operatedRef) {
       if(stack.getItem() instanceof VaultGearItem) {
           Item moduleItem = moduleData.getItemProvider().asItem();
           if(ModModuleToVaultGearModifications.supports(moduleItem)) {
               GearDataCache cache = GearDataCache.of(stack);
               if (cache.isModifiable()) {
                   ModModuleToVaultGearModifications.ModuleModifier<?> modifier = ModModuleToVaultGearModifications.getModification(moduleData.getItemProvider().asItem());
                   if (cache.hasAttribute(modifier.attribute())) {
                       return;
                   } else {
                       operatedRef.set(true);
                       operatingTicks++;
                       energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
                       if (operatingTicks == ticksRequired) {
                           operatingTicks = 0;
                           containerSlot.setStack(modifier.apply(stack));
                           MekanismUtils.logMismatchedStackSize(moduleSlot.shrinkStack(1, Action.EXECUTE), 1);
                       }
                   }
               }
           }
       }
   }

   @Inject(method = "removeModule", at = @At("HEAD"), cancellable = true)
    public void removeModule(Player player, ModuleData<?> type, CallbackInfo ci) {
        ItemStack stack = this.containerSlot.getStack();
        if(stack.getItem() instanceof VaultGearItem) {
            this.containerSlot.setStack(ModModuleToVaultGearModifications.removeModule(stack, type));
            ci.cancel();
        }
        if (!stack.isEmpty()) {
            IModuleContainerItem container = (IModuleContainerItem)stack.getItem();
            if (container.hasModule(stack, type) && player.getInventory().add(type.getItemProvider().getItemStack())) {
                container.removeModule(stack, type);
                this.containerSlot.setStack(stack);
            }
        }

    }
}

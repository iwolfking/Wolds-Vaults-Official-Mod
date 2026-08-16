package xyz.iwolfking.woldsvaults.mixins.mekanism;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.item.gear.VaultArmorItem;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = RadiationManager.class, remap = false)
public class MixinRadiationManager {
    @Inject(method = "getRadiationResistance", at = @At(value = "INVOKE", target = "Lmekanism/common/util/CapabilityUtils;getCapability(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"), cancellable = true)
    private void checkVaultGearRadiationImmunity(LivingEntity entity, CallbackInfoReturnable<Double> cir, @Local(name = "stack") ItemStack stack) {
        if(stack.getItem() instanceof VaultArmorItem) {
            GearDataCache dataCache = GearDataCache.of(stack);
            if(dataCache.hasAttribute(ModGearAttributes.RADIATION_IMMUNITY)) {
                cir.setReturnValue(Double.MAX_VALUE);
            }
        }
    }
}

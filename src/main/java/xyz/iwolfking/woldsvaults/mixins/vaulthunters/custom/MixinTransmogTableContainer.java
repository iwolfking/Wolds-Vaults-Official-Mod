package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.container.TransmogTableContainer;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TransmogTableContainer.class, remap = false)
public class MixinTransmogTableContainer {
    @WrapOperation(method = "lambda$isModelAllowed$2", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;getRarity()Liskallia/vault/gear/VaultGearRarity;"))
    private static VaultGearRarity dontReturnUniqueForEtchedGear(VaultGearData instance, Operation<VaultGearRarity> original) {
        if(instance.hasAttribute(ModGearAttributes.ETCHING)) {
            return VaultGearRarity.OMEGA;
        }

        return original.call(instance);
    }

    @WrapOperation(method = "getUniqueModelWhitelist", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;getRarity()Liskallia/vault/gear/VaultGearRarity;"))
    private static VaultGearRarity dontReturnUniqueForEtchedGearAgain(VaultGearData instance, Operation<VaultGearRarity> original) {
        if(instance.hasAttribute(ModGearAttributes.ETCHING)) {
            return VaultGearRarity.OMEGA;
        }

        return original.call(instance);
    }
}

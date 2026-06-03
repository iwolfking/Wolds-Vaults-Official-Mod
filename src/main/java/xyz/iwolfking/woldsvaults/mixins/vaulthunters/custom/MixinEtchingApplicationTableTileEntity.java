package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.EtchingApplicationTableTileEntity;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EtchingApplicationTableTileEntity.class, remap = false)
public class MixinEtchingApplicationTableTileEntity {
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/VaultGearRarity;equals(Ljava/lang/Object;)Z"))
    private boolean allowEtchingUniques(VaultGearRarity instance, Object o) {
        return false;
    }

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;isModifiable()Z"))
    private boolean allowEtchingUnmodifiable(VaultGearData instance) {
        return true;
    }

    @Redirect(method = "getCompatibility", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/VaultGearRarity;equals(Ljava/lang/Object;)Z"))
    private boolean allowEtchingUniquesCompat(VaultGearRarity instance, Object o) {
        return false;
    }

    @Redirect(method = "getCompatibility", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;isModifiable()Z"))
    private boolean allowEtchingUnmodifiableCompat(VaultGearData instance) {
        return true;
    }
}

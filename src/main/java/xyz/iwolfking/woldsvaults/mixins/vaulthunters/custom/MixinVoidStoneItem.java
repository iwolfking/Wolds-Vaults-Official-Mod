package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VoidStoneGearData;
import iskallia.vault.item.gear.VoidStoneItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = VoidStoneItem.class, remap = false)
public abstract class MixinVoidStoneItem {

    /**
     * @author iwolfking
     * @reason Make Void Stones always void items in vaults.
     */
    @Overwrite
    public static boolean isUsableInVault(ItemStack stack, UUID vaultId) {
        return true;
    }

    @Redirect(method = "canUnequip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowUnequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VoidStoneItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }

    @Redirect(method = "canEquip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowReequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VoidStoneItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }
}

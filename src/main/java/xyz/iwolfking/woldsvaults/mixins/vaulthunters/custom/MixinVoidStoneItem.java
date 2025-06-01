package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.gear.VoidStoneItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(value = VoidStoneItem.class, remap = false)
public class MixinVoidStoneItem {
    @Redirect(method = "canUnequip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowUnequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VoidStoneItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }

    @Redirect(method = "canEquip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowReequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VoidStoneItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }
}

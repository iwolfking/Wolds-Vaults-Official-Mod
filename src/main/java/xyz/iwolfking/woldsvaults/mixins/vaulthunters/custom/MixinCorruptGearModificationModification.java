package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.modification.operation.CorruptGearModification;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.gear.VaultNecklaceItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CorruptGearModification.class, remap = false)
public class MixinCorruptGearModificationModification {
    @WrapOperation(method = "canApply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), remap = true)
    private Item allowVaultPendants(ItemStack instance, Operation<Item> original) {
        if(instance.getItem() instanceof VaultNecklaceItem) {
            return ModItems.SWORD;
        }

        return original.call(instance);
    }
}

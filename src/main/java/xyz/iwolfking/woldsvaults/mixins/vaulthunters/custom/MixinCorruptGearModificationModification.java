package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import java.util.Random;
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
    /** Set pieces cannot be sealed. */
    @Inject(method = "canApply", at = @At("HEAD"), cancellable = true)
    private void blockSetPieces(ItemStack stack, ItemStack materialStack, Player player, Random rand, CallbackInfoReturnable<GearModification.Result> cir) {
        if (GearSetHelper.isSetPiece(stack)) {
            cir.setReturnValue(GearModification.Result.errorUnmodifiable());
        }
    }

    @WrapOperation(method = "canApply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), remap = true)
    private Item allowVaultPendants(ItemStack instance, Operation<Item> original) {
        if(instance.getItem() instanceof VaultNecklaceItem) {
            return ModItems.SWORD;
        }

        return original.call(instance);
    }
}

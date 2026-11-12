package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/** Stamps black market gear with a provenance attribute the ancient unique roll reads. Non-gear is untouched. */
@Mixin(targets = "iskallia.vault.world.data.PlayerBlackMarketData$BlackMarket$SelectedTrade", remap = false)
public abstract class MixinBlackMarketSelectedTrade {
    @WrapOperation(method = "initialize",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/util/LootInitialization;initializeVaultLoot(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack woldsvaults$stampBlackMarketOrigin(ItemStack stack, int level, Operation<ItemStack> original) {
        ItemStack initialized = original.call(stack, level);
        if (initialized == null || !(initialized.getItem() instanceof VaultGearItem)) {
            return initialized;
        }
        VaultGearData data = VaultGearData.read(initialized);
        data.createOrReplaceAttributeValue(ModGearAttributes.BLACK_MARKET_ORIGIN, true);
        data.write(initialized);
        return initialized;
    }
}

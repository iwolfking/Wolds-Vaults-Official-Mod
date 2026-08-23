package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/**
 * Stamps black market gear with its origin so ancient uniques can recognise it.
 *
 * <p>A black market offer is built through {@code LootInitialization.initializeVaultLoot} with no
 * vault, so it never receives the base mod's {@code IS_LOOT} stamp and is indistinguishable from a
 * crafted unique by the time it is identified. The rework wants black market uniques eligible for
 * the ancient roll while crafted and vendor uniques stay out, which needs a stamp of its own.
 *
 * <p>It is written as a gear attribute rather than loose stack NBT because that is what survives
 * the trip from an unidentified pool item to the identified unique - the same reason the base mod
 * carries provenance that way. Non-gear trades are left untouched.
 */
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

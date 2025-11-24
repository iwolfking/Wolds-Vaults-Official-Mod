package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.recycler;

import iskallia.vault.dynamodel.DynamicModelItem;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.item.IAnvilPreventCombination;
import iskallia.vault.item.IConditionalDamageable;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.gear.RecyclableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = VaultGearItem.class, remap = false)
public interface MixinVaultGearItem extends IForgeItem, VaultGearTooltipItem, DataTransferItem, VaultLevelItem, RecyclableItem, DynamicModelItem, IConditionalDamageable, IAnvilPreventCombination, IdentifiableItem {

    @Override
    default boolean shouldPreventAnvilCombination(ItemStack other) {
        return true;
    }

}

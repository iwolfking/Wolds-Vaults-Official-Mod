package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.recycler;

import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.config.entry.ChanceItemStackEntry;
import iskallia.vault.config.entry.ConditionalChanceItemStackEntry;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.dynamodel.DynamicModelItem;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.IAnvilPreventCombination;
import iskallia.vault.item.IConditionalDamageable;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.gear.RecyclableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.MixinVaultGearRarity;

@Mixin(value = VaultGearItem.class, remap = false)
public interface MixinVaultGearItem extends IForgeItem, VaultGearTooltipItem, DataTransferItem, VaultLevelItem, RecyclableItem, DynamicModelItem, IConditionalDamageable, IAnvilPreventCombination, IdentifiableItem {

    @Override
    public default boolean shouldPreventAnvilCombination(ItemStack other) {
        return true;
    }

//    /**
//     * @author iwolfking
//     * @reason Give specific Faceted Focus when a legendary modifier is present
//     */
//    @Overwrite
//    default VaultRecyclerConfig.RecyclerOutput getOutput(ItemStack input) {
//        VaultGearData data = VaultGearData.read(input);
//        VaultGearModifier<?> legMod = null;
//        for (VaultGearModifier<?> mod : data.getAllModifierAffixes()) {
//            if (mod.hasCategory(VaultGearModifier.AffixCategory.LEGENDARY)) {
//                legMod = mod;
//            }
//        }
//
//        boolean isUniqueOrHighRarity = data.getRarity().equals(VaultGearRarity.UNIQUE) || data.getRarity().equals(VaultGearRarity.valueOf("Mythic"));
//
//
//        if(legMod != null) {
//            if(VaultGearTierConfig.getConfig(input).isPresent()) {
//                VaultGearTierConfig tierConfig = VaultGearTierConfig.getConfig(input).get();
//                VaultGearTierConfig.ModifierTierGroup group = tierConfig.getTierGroup(legMod.getModifierIdentifier());
//                if(isUniqueOrHighRarity) {
//                    return new VaultRecyclerConfig.RecyclerOutput(new ChanceItemStackEntry(new ItemStack(ModItems.VAULT_SCRAP), 8, 16, 1.0F), new ConditionalChanceItemStackEntry(new ItemStack(ModItems.FACETED_FOCUS), 1, 1, 1.0F), new ConditionalChanceItemStackEntry(new ItemStack(ModItems.FACETED_FOCUS), 1, 1, 1.0F));
//                }
//
//                return new VaultRecyclerConfig.RecyclerOutput(new ChanceItemStackEntry(new ItemStack(ModItems.VAULT_SCRAP), 4, 8, 1.0F), new ConditionalChanceItemStackEntry(new ItemStack(Items.NETHERITE_SCRAP), 1, 3, 0.2F), new ConditionalChanceItemStackEntry(new ItemStack(ModItems.FACETED_FOCUS), 1, 1, 1.0F));
//            }
//        }
//
//        if(isUniqueOrHighRarity) {
//            return new VaultRecyclerConfig.RecyclerOutput(new ChanceItemStackEntry(new ItemStack(ModItems.VAULT_SCRAP), 8, 16, 1.0F), new ConditionalChanceItemStackEntry(new ItemStack(Items.NETHERITE_SCRAP), 2, 6, 0.4F), new ConditionalChanceItemStackEntry(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.CHUNK_OF_POWER), 1, 1, 0.25F));
//        }
//
//
//        return ModConfigs.VAULT_RECYCLER.getGearRecyclingOutput();
//    }
}

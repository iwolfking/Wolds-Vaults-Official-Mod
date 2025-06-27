package xyz.iwolfking.woldsvaults.enchantment;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class DecorShieldEnchantment extends Enchantment {
    public DecorShieldEnchantment(Rarity pRarity, EquipmentSlot... pApplicableSlots) {
        super(pRarity, MAGNET_ONLY, pApplicableSlots);
        this.setRegistryName(WoldsVaults.id("decor_shield"));
    }

    public static final EnchantmentCategory MAGNET_ONLY = EnchantmentCategory.create("magnet", item -> item == ModItems.MAGNET);

    @Override public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem() == ModItems.MAGNET;
    }

    public static final TagKey<Item> DECOR_ITEMS = ItemTags.create(VaultMod.id("decor"));

    @SuppressWarnings("deprecation")
    public static boolean isDecorItem(Item item) {
        return item.builtInRegistryHolder().is(DECOR_ITEMS);
    }

    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override public boolean isTradeable() {
        return false;
    }

    @Override public boolean isDiscoverable() {
        return false;
    }
}

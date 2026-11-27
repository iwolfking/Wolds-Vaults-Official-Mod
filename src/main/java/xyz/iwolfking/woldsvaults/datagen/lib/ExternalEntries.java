package xyz.iwolfking.woldsvaults.datagen.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Stand-ins for registry entries of mods that are not on the datagen classpath. They are never registered and
 * only carry a registry name, which is all the Vault Hunters config adapters write, so providers can reference
 * those mods' ids without a registry lookup that would resolve to null. The stand-in types are named classes
 * because Gson excludes anonymous classes, which turns them into null map keys.
 */
public final class ExternalEntries {
    private ExternalEntries() {
    }

    public static Enchantment enchantment(String namespace, String path) {
        Enchantment enchantment = new ExternalEnchantment();
        enchantment.setRegistryName(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return enchantment;
    }

    public static ItemStack item(String namespace, String path, int count) {
        Item item = new Item(new Item.Properties());
        item.setRegistryName(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return new ItemStack(item, count);
    }

    private static final class ExternalEnchantment extends Enchantment {
        private ExternalEnchantment() {
            super(Enchantment.Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[0]);
        }
    }
}

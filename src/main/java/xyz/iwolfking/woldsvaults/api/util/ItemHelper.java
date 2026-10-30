package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ItemHelper {
    public static <T extends Item> List<T> getAllRegisteredItems(Predicate<Item> itemPredicate, List<String> namespacesToExclude, Class<T> type) {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(itemPredicate)
                .filter(b -> b.getRegistryName() != null && !namespacesToExclude.contains(b.getRegistryName().getNamespace()))
                .sorted(Comparator.comparing(b -> b.getRegistryName() != null ? b.getRegistryName().toString() : ""))
                .map(type::cast)
                .toList();
    }

    public static <T extends Item> List<T> getAllRegisteredItems(Predicate<Item> itemPredicate, Class<T> type) {
        return getAllRegisteredItems(itemPredicate, List.of(), type);
    }

    public static List<Item> getAllRegisteredItems(Predicate<Item> itemPredicate) {
        return getAllRegisteredItems(itemPredicate, List.of(), Item.class);
    }
}

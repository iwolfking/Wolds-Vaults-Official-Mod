package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public static <T> T[] getItemsOfClassFrom(Class<T> type, Supplier<T[]> instance, Class<?> classToSearch) {
        List<T> items = new ArrayList<>();

        for (Field field : classToSearch.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    items.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return items.toArray(instance.get());
    }

    public static <T> T[] getItemsContaining(String text, Supplier<T[]> instance, Class<?> classToSearch) {
        List<T> items = new ArrayList<>();

        for (Field field : classToSearch.getDeclaredFields()) {
            if (field.getName().contains(text)) {
                field.setAccessible(true);
                try {
                    items.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return items.toArray(instance.get());
    }
}

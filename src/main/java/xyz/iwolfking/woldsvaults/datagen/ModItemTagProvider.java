package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.item.BasicItem;
import iskallia.vault.item.ItemVaultCrystalSeal;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModTags;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(DataGenerator pGenerator, BlockTagsProvider pBlockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, pBlockTagsProvider, WoldsVaults.MOD_ID, existingFileHelper);
    }


    @Override
    protected void addTags() {
        tag(iskallia.vault.init.ModItems.CRYSTAL_SEALS_TAG)
                .add(getItemsOfClass(ItemVaultCrystalSeal.class, () -> new ItemVaultCrystalSeal[]{}));
        tag(iskallia.vault.init.ModItems.CRYSTAL_CAPSTONES_TAG)
                .add(getItemsContaining("CAPSTONE", () -> new BasicItem[]{}));
        tag(ModTags.PLAYER_GEMS)
                .add(iskallia.vault.init.ModItems.ISKALLIUM_GEM)
                .add(iskallia.vault.init.ModItems.UPALINE_GEM)
                .add(iskallia.vault.init.ModItems.ASHIUM_GEM)
                .add(iskallia.vault.init.ModItems.XENIUM_GEM)
                .add(iskallia.vault.init.ModItems.GORGINITE_GEM)
                .add(iskallia.vault.init.ModItems.BOMIGNITE_GEM)
                .add(iskallia.vault.init.ModItems.SPARKLETINE_GEM)
                .add(iskallia.vault.init.ModItems.PETZANITE_GEM)
                .add(iskallia.vault.init.ModItems.TUBIUM_GEM);
        tag(ModTags.ALCHEMY_INGREDIENT).add(getItemsOfClass(AlchemyIngredientItem.class, () -> new AlchemyIngredientItem[]{}));
        tag(ModTags.ALCHEMY_CATALYST).add(getItemsOfClass(CatalystItem.class, () -> new CatalystItem[]{}));
    }

    public static <T> T[] getItemsOfClass(Class<T> type, Supplier<T[]> instance) {
        List<T> items = new ArrayList<>();

        for (Field field : ModItems.class.getDeclaredFields()) {
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

    public static <T> T[] getItemsContaining(String text, Supplier<T[]> instance) {
        List<T> items = new ArrayList<>();

        for (Field field : ModItems.class.getDeclaredFields()) {
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

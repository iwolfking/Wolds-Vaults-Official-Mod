package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.item.BossRuneItem;
import net.joseph.vaultfilters.attributes.abstracts.StringListAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OfferingItemAttribute extends StringListAttribute {
    public OfferingItemAttribute(String value) {
        super(value);
    }

    public String getTranslationKey() {
        return "offering_items";
    }

    public String getValue(ItemStack itemStack) {
        return "";
    }

    @Override
    public Object[] getTranslationParameters() {
        String modifiedItemName = (this.value).replace("[", "").replace("]", "").trim();
        return new Object[]{modifiedItemName};
    }

    public static List<String> getOfferingItems(ItemStack stack) {
        List<String> itemNamesList = new ArrayList<>();
        if (stack.getItem() instanceof BossRuneItem) {
            if(!stack.getOrCreateTag().contains("Items")) {
                return List.of();
            }

            ListTag items = stack.getOrCreateTag().getList("Items", 10);

            for(Tag tag : items) {
                itemNamesList.add(ItemStack.of((CompoundTag) tag).getDisplayName().getString());
            }

        }

        return itemNamesList;
    }

    public List<String> getValues(ItemStack itemStack) {
        return getOfferingItems(itemStack);
    }
}

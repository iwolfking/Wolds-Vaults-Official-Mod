package xyz.iwolfking.woldsvaults.items.alchemy;

import iskallia.vault.item.BasicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DecoPotionItem extends BasicItem {
    private Type type = Type.UNKNOWN;

    public DecoPotionItem(ResourceLocation id) {
        super(id);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTypeBasedOnContents(List<ItemStack> items) {
        if (items.size() == 1) {
            this.setType(DecoPotionItem.Type.ONE_INGREDIENT);
        } else if (items.size() == 2) {
            this.setType(DecoPotionItem.Type.TWO_INGREDIENT);
        } else if (items.size() == 3) {
            this.setType(DecoPotionItem.Type.THREE_INGREDIENT);
        } else {
            this.setType(Type.UNKNOWN);
        }

    }


    public static void setColor(ItemStack stack, int color) {
        stack.getOrCreateTag().putInt("PotionColor", color);
    }

    public enum Type {
        UNKNOWN,
        ONE_INGREDIENT,
        TWO_INGREDIENT,
        THREE_INGREDIENT,
        BREWING
    }
}

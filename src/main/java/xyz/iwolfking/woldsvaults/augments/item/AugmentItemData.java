package xyz.iwolfking.woldsvaults.augments.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.cast.SpellPlan;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The augments rolled onto a wand or focus and the recipes its owner arranged from them, stored in the item's NBT.
 * Each rolled augment is a number of tokens; every glyph placed in any recipe on the item uses one token, so an item
 * with one Shotgun can put Shotgun on one ability only.
 */
public final class AugmentItemData {
    public static final String TAG = "woldsvaults_augments";
    private static final String ROLLED = "rolled";
    private static final String RECIPES = "recipes";

    private AugmentItemData() {
    }

    public static boolean hasRolled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getCompound(TAG).contains(ROLLED);
    }

    public static Map<AugmentType, Integer> rolled(ItemStack stack) {
        Map<AugmentType, Integer> rolled = new EnumMap<>(AugmentType.class);
        if (!stack.hasTag()) {
            return rolled;
        }
        CompoundTag tag = stack.getTag().getCompound(TAG).getCompound(ROLLED);
        for (String key : tag.getAllKeys()) {
            AugmentType type = AugmentType.byId(key);
            if (type == null) {
                WoldsVaults.LOGGER.error("Focus augments: item carries unknown augment {}; ignoring it", key);
                continue;
            }
            rolled.put(type, Math.max(0, tag.getInt(key)));
        }
        return rolled;
    }

    public static void setRolled(ItemStack stack, Map<AugmentType, Integer> rolled) {
        CompoundTag root = stack.getOrCreateTag().getCompound(TAG);
        CompoundTag tag = new CompoundTag();
        rolled.forEach((type, count) -> {
            if (count > 0) {
                tag.putInt(type.id(), count);
            }
        });
        root.put(ROLLED, tag);
        stack.getOrCreateTag().put(TAG, root);
    }

    public static Map<String, List<String>> recipes(ItemStack stack) {
        if (!stack.hasTag()) {
            return Collections.emptyMap();
        }
        CompoundTag tag = stack.getTag().getCompound(TAG).getCompound(RECIPES);
        Map<String, List<String>> recipes = new LinkedHashMap<>();
        for (String ability : tag.getAllKeys()) {
            List<String> glyphs = new ArrayList<>();
            for (Tag glyph : tag.getList(ability, Tag.TAG_STRING)) {
                glyphs.add(glyph.getAsString());
            }
            recipes.put(ability, glyphs);
        }
        return recipes;
    }

    @Nullable
    public static List<String> recipe(ItemStack stack, String ability) {
        return recipes(stack).get(ability);
    }

    public static void setRecipe(ItemStack stack, String ability, @Nullable List<String> glyphs) {
        CompoundTag root = stack.getOrCreateTag().getCompound(TAG);
        CompoundTag tag = root.getCompound(RECIPES);
        if (glyphs == null) {
            tag.remove(ability);
        } else {
            ListTag list = new ListTag();
            glyphs.forEach(glyph -> list.add(StringTag.valueOf(glyph)));
            tag.put(ability, list);
        }
        root.put(RECIPES, tag);
        stack.getOrCreateTag().put(TAG, root);
    }

    /**
     * Whether the item can afford all of its recipes: no more recipes than the table allows, every recipe valid,
     * and no augment placed more often than the item rolled it. Null when it can, otherwise the reason.
     */
    @Nullable
    public static String checkBudget(FocusAugmentsConfig config, ItemStack stack, Map<String, List<String>> recipes) {
        if (recipes.size() > config.limits.maxRecipesPerItem) {
            return "an item holds at most " + config.limits.maxRecipesPerItem + " recipes";
        }
        Map<AugmentType, Integer> used = new EnumMap<>(AugmentType.class);
        for (Map.Entry<String, List<String>> recipe : recipes.entrySet()) {
            SpellPlan plan = SpellPlan.parse(config, recipe.getKey(), recipe.getValue());
            if (!plan.isValid()) {
                return "the recipe on " + recipe.getKey() + " is invalid: " + String.join("; ", plan.errors());
            }
            plan.tokens().forEach((type, count) -> used.merge(type, count, Integer::sum));
        }
        Map<AugmentType, Integer> rolled = rolled(stack);
        for (Map.Entry<AugmentType, Integer> use : used.entrySet()) {
            int have = rolled.getOrDefault(use.getKey(), 0);
            if (use.getValue() > have) {
                return "the recipes use " + use.getValue() + " " + use.getKey().displayName() + " but the item has " + have;
            }
        }
        return null;
    }
}

package xyz.iwolfking.woldsvaults.integration.jei.category.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.vending.ProductEntry;
import iskallia.vault.util.data.WeightedList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class GenericLootableBoxCategory implements IRecipeCategory<GenericLootableConfig> {
    private static final ResourceLocation TEXTURE = VaultMod.id("textures/gui/jei/loot_info.png");
    private final IDrawable background;
    private final IDrawable icon;
    private final GenericLootableConfig configInstance;
    private final TextComponent title;
    private final RecipeType<GenericLootableConfig> recipeType;


    public GenericLootableBoxCategory(IGuiHelper guiHelper, GenericLootableConfig configInstance, TextComponent title, Item itemForIcon, RecipeType<GenericLootableConfig> recipeType) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 162, 108);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemForIcon.getDefaultInstance());
        this.configInstance = configInstance;
        this.title = title;
        this.recipeType = recipeType;
    }

    @Nonnull
    public Component getTitle() {
        return title;
    }

    @Nonnull
    public IDrawable getBackground() {
        return this.background;
    }

    @Nonnull
    public IDrawable getIcon() {
        return this.icon;
    }

    @Nonnull
    @Override
    public RecipeType<GenericLootableConfig> getRecipeType() {
        return this.recipeType;
    }

    @Nonnull @SuppressWarnings("removal")
    public ResourceLocation getUid() {
        return this.getRecipeType().getUid();
    }

    @Nonnull @SuppressWarnings("removal")
    public Class<? extends GenericLootableConfig> getRecipeClass() {
        return this.getRecipeType().getRecipeClass();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setRecipe(IRecipeLayoutBuilder builder, GenericLootableConfig recipe, IFocusGroup focuses) {
        List<ItemStack> itemList = new ArrayList<>();
        recipe.POOL.forEach((productEntry, aDouble) -> itemList.add(productEntry.generateItemStack()));

        int count = itemList.size();

        for(int i = 0; i < count; ++i) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 1 + 18 * (i % 9), 1 + 18 * (i / 9)).addItemStack(addChanceTooltip(itemList.get(i)));
        }

    }

    public ItemStack addChanceTooltip(ItemStack stack)
    {
        double totalWeight = this.configInstance.POOL.getTotalWeight();

        WeightedList<ProductEntry> entries = this.configInstance.POOL;
        for(WeightedList.Entry<ProductEntry> entry : entries) {
            if(entry.value.getNBT().equals(stack.getTag())) {
                CompoundTag nbt = stack.getOrCreateTagElement("display");
                ListTag list = nbt.getList("Lore", 8);
                MutableComponent component = new TextComponent("Chance: ");
                double chance = (entry.weight / totalWeight) * 100;
                component.append(String.format("%.2f", chance));
                component.append("%");
                list.add(StringTag.valueOf(Component.Serializer.toJson(component.withStyle(ChatFormatting.YELLOW))));
                nbt.put("Lore", list);
            }
        }

        return stack;
    }
}

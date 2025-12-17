package xyz.iwolfking.woldsvaults.recipes.gear;

import cofh.ensorcellation.init.EnsorcEnchantments;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.item.tool.ToolItem;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.gear.VaultBattleStaffItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultRangItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MercyEnchantmentAdderRecipe extends VanillaAnvilRecipe {


    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if ((primary.getItem() instanceof SwordItem || primary.getItem() instanceof AxeItem || primary.getItem() instanceof ToolItem || primary.getItem() instanceof VaultBattleStaffItem || primary.getItem() instanceof TridentItem || primary.getItem() instanceof VaultRangItem) && secondary.getItem() == ModItems.MERCY_ORB) {
            ItemStack output = primary.copy();
            Map<Enchantment, Integer> stackEnchMap = EnchantmentHelper.getEnchantments(output);
            if(stackEnchMap.containsKey(EnsorcEnchantments.CURSE_MERCY.get())) {
                return false;
            }


            context.onTake(context.getTake().append(() -> {
                output.enchant(EnsorcEnchantments.CURSE_MERCY.get(), 1);
                context.setOutput(output);
                context.getInput()[0].shrink(1);
                context.getInput()[1].shrink(1);
            }));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRegisterJEI(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();

        List<ItemStack> inputs = List.of(new ItemStack(ModItems.RANG), new ItemStack(ModItems.BATTLESTAFF), new ItemStack(ModItems.TRIDENT), new ItemStack(iskallia.vault.init.ModItems.SWORD), new ItemStack(iskallia.vault.init.ModItems.AXE));
        ItemStack secondary = new ItemStack(ModItems.MERCY_ORB);
        List<ItemStack> outputs = new ArrayList<>();
        for(ItemStack input : inputs) {
            ItemStack output = input.copy();
            EnchantmentHelper.setEnchantments(Map.of(EnsorcEnchantments.CURSE_MERCY.get(), 1), output);
            outputs.add(output);
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, List.of(secondary), outputs)));
    }
}

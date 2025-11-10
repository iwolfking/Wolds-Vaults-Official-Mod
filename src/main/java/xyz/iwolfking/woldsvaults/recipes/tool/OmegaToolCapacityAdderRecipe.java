package xyz.iwolfking.woldsvaults.recipes.tool;

import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;

public class OmegaToolCapacityAdderRecipe extends VanillaAnvilRecipe {


    @Override
    public boolean onSimpleCraft(AnvilContext context) {

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof ToolItem && secondary.getItem() == ModItems.CRYSTAL_REINFORCEMENT) {
            ItemStack output = primary.copy();
            VaultGearData gear = ToolGearData.read(output);
            int toolCapacity = 0;
            if(gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).isPresent()) {
                toolCapacity = gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).get();
            }
            else {
                return false;
            }


            gear.createOrReplaceAttributeValue(ModGearAttributes.TOOL_CAPACITY, toolCapacity + 10);

            gear.write(output);
            context.setOutput(output);

            context.onTake(context.getTake().append(() -> {
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
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        List<ItemStack> secondary = List.of(new ItemStack(ModItems.CRYSTAL_REINFORCEMENT));

        for(ToolMaterial material : ToolMaterial.values()) {
            for(ToolType type : ToolType.values()) {
                inputs.add(ToolItem.create(material, type));
                ItemStack output = ToolItem.create(material, type);
                ToolItem.addCapacity(output, 10);
                outputs.add(output);
            }
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, secondary, outputs)));
    }
}

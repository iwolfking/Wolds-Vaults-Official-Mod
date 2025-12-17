package xyz.iwolfking.woldsvaults.recipes.tool;

import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;

public class ToolStylishAdderRecipe extends VanillaAnvilRecipe {


    @Override
    public boolean onSimpleCraft(AnvilContext context) {

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof ToolItem && secondary.getItem() == ModItems.STYLISH_FOCUS) {
            ItemStack output = primary.copy();
            VaultGearData gear = ToolGearData.read(output);
            if(gear.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ROTATING_TOOL).isPresent()) {
                return false;
            }
            else {
                gear.addModifier(VaultGearModifier.AffixType.SUFFIX, new VaultGearModifier<>(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ROTATING_TOOL, true));
            }

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
        List<ItemStack> secondary = List.of(new ItemStack(ModItems.STYLISH_FOCUS));

        for(ToolMaterial material : ToolMaterial.values()) {
            for(ToolType type : ToolType.values()) {
                inputs.add(ToolItem.create(material, type));
                ItemStack output = ToolItem.create(material, type);
                VaultGearData data = VaultGearData.read(output);
                data.createOrReplaceAttributeValue(ModGearAttributes.ROTATING_TOOL, true);
                data.write(output);
                outputs.add(output);
            }
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, secondary, outputs)));
    }
}

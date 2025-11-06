package xyz.iwolfking.woldsvaults.recipes.tool;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ToolModifierNullifyingItem;

import java.util.ArrayList;
import java.util.List;

public class ToolModifierNullifierRecipe extends VanillaAnvilRecipe {


    @Override
    public boolean onSimpleCraft(AnvilContext context) {

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof ToolItem && secondary.getItem() instanceof ToolModifierNullifyingItem) {
            ItemStack output = primary.copy();
            ItemStack input = secondary.copy();
            VaultGearAttribute<?> attributeToRemove = ToolModifierNullifyingItem.getModifierTag(input);
            if(attributeToRemove == null) {
                return false;
            }
            VaultGearData gear = ToolGearData.read(output);
            List<VaultGearModifier<?>> modifiersRemoved = new ArrayList<>();

            for (VaultGearModifier<?> mod : gear.getAllModifierAffixes()) {
                if (mod.getAttribute().equals(attributeToRemove)) {
                    modifiersRemoved.add(mod);
                }
            }

            if(modifiersRemoved.isEmpty()) {
                return false;
            }

            for(VaultGearModifier<?> mod : modifiersRemoved) {
                gear.removeModifier(mod);
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
        List<ItemStack> secondaries = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for(String modifier : ToolModifierNullifyingItem.CHISELING_MODIFIER_TYPES) {
            secondaries.add(ToolModifierNullifyingItem.create(modifier));
        }

        for(ToolMaterial material : ToolMaterial.values()) {
            for(ToolType type : ToolType.values()) {
                ItemStack input = ToolItem.create(material, type);
                inputs.add(input);
                ItemStack output = ToolItem.create(material, type);
                outputs.add(output);
            }
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, secondaries, outputs)));
    }
}

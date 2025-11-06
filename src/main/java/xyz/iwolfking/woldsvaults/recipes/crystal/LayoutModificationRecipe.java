package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

import java.util.ArrayList;
import java.util.List;

public class LayoutModificationRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultCrystalItem && secondary.getItem() == ModItems.LAYOUT_MANIPULATOR) {
            ItemStack output = primary.copy();
            CrystalData data = CrystalData.read(output);

            if(data.getProperties().isUnmodifiable()) {
                return false;
            }

            if(LayoutModificationItem.getLayout(secondary).isEmpty()) {
                return false;
            }

            CrystalLayout layout =  LayoutModificationItem.getLayout(secondary).get();

            data.setLayout(layout);

            data.write(output);
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
        List<ItemStack> outputs = new ArrayList<>();

        ItemStack crystal = VaultCrystalItem.create(crystalData -> {
            crystalData.getProperties().setLevel(0);
        });

        List<ItemStack> secondaries = new ArrayList<>();

        secondaries.add(LayoutModificationItem.create("infinite", 1, 1));
        secondaries.add(LayoutModificationItem.create("circle", 1, 8));
        secondaries.add(LayoutModificationItem.create("polygon", 1, 12));
        secondaries.add(LayoutModificationItem.create("spiral", 1, 4));

        for(ItemStack secondary : secondaries) {
            ItemStack crystalOutput = VaultCrystalItem.create(crystalData -> {
                crystalData.getProperties().setLevel(0);
                CrystalLayout layout = LayoutModificationItem.getLayout(secondary).orElse(new ClassicInfiniteCrystalLayout(1));
                crystalData.setLayout(layout);
            });

            outputs.add(crystalOutput);

        }


        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(List.of(crystal), secondaries, outputs)));
    }

}

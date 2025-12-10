package xyz.iwolfking.woldsvaults.recipes.capstone;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

public class MotePurityCapstoneRecipe extends VanillaAnvilRecipe {
    public MotePurityCapstoneRecipe() {
    }

    public boolean onSimpleCraft(AnvilContext context) {
        if(!WoldsVaultsConfig.COMMON.enableMoteRecipes.get()) {
            return false;
        }
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() == ModItems.VAULT_CRYSTAL && secondary.getItem() == ModItems.MOTE_PURITY) {
            ItemStack output = primary.copy();
            CrystalData crystal = CrystalData.read(output);
            VaultModifierRegistry.getOpt(VaultMod.id("purified")).ifPresent(modifier -> {
                VaultModifierStack modifierStack = VaultModifierStack.of(modifier);
                if (crystal.addModifierByCrafting(modifierStack, false, true)) {
                    crystal.addModifierByCrafting(modifierStack, false, false);
                    crystal.getModifiers().removeRandomCurse();
                    crystal.getProperties().setUnmodifiable(true);
                    crystal.write(output);
                }

            });
            crystal.write(output);
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

    public void onRegisterJEI(IRecipeRegistration registry) {
    }
}

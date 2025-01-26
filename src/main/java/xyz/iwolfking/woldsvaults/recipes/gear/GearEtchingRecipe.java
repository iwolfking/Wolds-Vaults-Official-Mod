package xyz.iwolfking.woldsvaults.recipes.gear;

import iskallia.vault.config.EtchingConfig;
import iskallia.vault.etching.EtchingHelper;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.etching.set.DryadSet;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import iskallia.vault.item.gear.EtchingItem;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;

import javax.management.Attribute;

public class GearEtchingRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultGearItem gearItem && secondary.getItem() == ModItems.ETCHING) {
            ItemStack output = primary.copy();
            AttributeGearData data = AttributeGearData.read(secondary);
            VaultGearData gear = VaultGearData.read(primary);
            EtchingSet<?> etchingSet = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);

            if(etchingSet == null) {
                return false;
            }

            if(!gear.isModifiable()) {
                return false;
            }

            if(!gearItem.getClassification(primary).equals(VaultGearClassification.ARMOR)) {
                return false;
            }

            gear.createOrReplaceAttributeValue(ModGearAttributes.ETCHING, etchingSet);

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
    public void onRegisterJEI(IRecipeRegistration iRecipeRegistration) {

    }
}

package xyz.iwolfking.woldsvaults.recipes.gear;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class GearRepairAdderRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultGearItem && secondary.getItem() == ModItems.REPAIR_AUGMENTER) {
            ItemStack output = primary.copy();
            VaultGearData gear = VaultGearData.read(output);
            int currentRepairSlotCount = gear.getRepairSlots();
            //If at the max number of slots already, don't allow any more.
            if(currentRepairSlotCount >= 10) {
                return false;
            }

            gear.setRepairSlots(currentRepairSlotCount + 1);
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

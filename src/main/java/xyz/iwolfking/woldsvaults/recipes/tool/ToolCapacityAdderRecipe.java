package xyz.iwolfking.woldsvaults.recipes.tool;

import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.VanillaAnvilRecipe;
import iskallia.vault.item.tool.ToolItem;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ToolCapacityAdderRecipe extends VanillaAnvilRecipe {

    private static final String REINFORCEMENT_COUNT_TAG = "woldsvaults.reinforcements_applied";

    @Override
    public boolean onSimpleCraft(AnvilContext context) {

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof ToolItem tool && secondary.getItem() == ModItems.RESONATING_REINFORCEMENT) {
            ItemStack output = primary.copy();
            VaultGearData gear = ToolGearData.read(output);
            int toolCapacity = 0;
            if(gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).isPresent()) {
                toolCapacity = gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).get();
            }
            else {
                return false;
            }

            CompoundTag tag = output.getOrCreateTag();
            int reinforcementsApplied = 0;

            if (tag.contains(REINFORCEMENT_COUNT_TAG)) {
                reinforcementsApplied = tag.getInt(REINFORCEMENT_COUNT_TAG);
            }

            if (reinforcementsApplied >= 2) {
                return false;
            }

            gear.createOrReplaceAttributeValue(ModGearAttributes.TOOL_CAPACITY, toolCapacity + 10);
            gear.write(output);

            reinforcementsApplied++;
            tag.putInt(REINFORCEMENT_COUNT_TAG, reinforcementsApplied);

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

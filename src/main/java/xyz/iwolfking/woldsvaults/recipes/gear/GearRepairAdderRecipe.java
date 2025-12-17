package xyz.iwolfking.woldsvaults.recipes.gear;

import cofh.ensorcellation.init.EnsorcEnchantments;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.item.gear.VaultCharmItem;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            if(currentRepairSlotCount >= 10 || !gear.isModifiable()) {
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
    public void onRegisterJEI(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();

        List<ItemStack> repairableItems = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof VaultGearItem vaultGearItem && vaultGearItem.canStoreRepairSlots(item.getDefaultInstance()))
                .map(Item::getDefaultInstance)
                .peek(stack -> {
                    if(stack.getItem() instanceof IdentifiableItem identifiableItem && !(stack.getItem() instanceof VaultCharmItem || stack.getItem() instanceof VaultMapItem)) {
                        identifiableItem.instantIdentify(null, stack);
                    }
                })
                .toList();



        ItemStack secondary = new ItemStack(ModItems.REPAIR_AUGMENTER);
        List<ItemStack> outputs = new ArrayList<>();
        for(ItemStack input : repairableItems) {
            VaultGearData data = VaultGearData.read(input);
            data.setRepairSlots(data.getRepairSlots() + 1);
            ItemStack output = input.copy();
            data.write(output);
            outputs.add(output);
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(repairableItems, List.of(secondary), outputs)));
    }
}

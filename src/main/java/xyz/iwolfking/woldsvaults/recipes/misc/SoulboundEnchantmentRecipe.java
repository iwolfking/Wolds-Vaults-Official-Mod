package xyz.iwolfking.woldsvaults.recipes.misc;

import cofh.ensorcellation.init.EnsorcEnchantments;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.AntiqueStampCollectorBook;
import iskallia.vault.item.CompassItem;
import iskallia.vault.item.ItemShardPouch;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.item.tool.JewelItem;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import xyz.iwolfking.woldsvaults.items.FilterNecklaceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoulboundEnchantmentRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext anvilContext) {
        ItemStack primary = anvilContext.getInput()[0];
        ItemStack secondary = anvilContext.getInput()[1];

        if(isValidItem(primary.getItem()) && secondary.getItem() instanceof JewelItem) {
            VaultGearData jewelData = VaultGearData.read(secondary);
            if(jewelData.hasAttribute(ModGearAttributes.SOULBOUND)) {
                ItemStack output = primary.copy();
                EnchantmentHelper.setEnchantments(Map.of(EnsorcEnchantments.SOULBOUND.get(), 1), output);
                anvilContext.setOutput(output);
                anvilContext.onTake(anvilContext.getTake().append(() -> {
                    anvilContext.getInput()[0].shrink(1);
                    anvilContext.getInput()[1].shrink(1);
                }));
                return true;
            }

        }

        return false;
    }

    @Override
    public void onRegisterJEI(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();

        List<ItemStack> inputs = List.of(new ItemStack(ModItems.SHARD_POUCH), new ItemStack(ModItems.VAULT_COMPASS), new ItemStack(ModItems.ANTIQUE_COLLECTOR_BOOK), new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.FILTER_NECKLACE));
        List<ItemStack> outputs = new ArrayList<>();
        ItemStack secondary = JewelItem.create((vaultGearData -> {
            vaultGearData.setRarity(VaultGearRarity.SCRAPPY);
            vaultGearData.setState(VaultGearState.IDENTIFIED);
            vaultGearData.addModifier(VaultGearModifier.AffixType.SUFFIX, new VaultGearModifier<>(ModGearAttributes.SOULBOUND, true));
            vaultGearData.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<>(ModGearAttributes.JEWEL_SIZE, 10));
        }));

        for(ItemStack input : inputs) {
            ItemStack output = input.copy();
            EnchantmentHelper.setEnchantments(Map.of(EnsorcEnchantments.SOULBOUND.get(), 1), output);
            outputs.add(output);
        }
        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, List.of(secondary), outputs)));
    }

    public boolean isValidItem(Item item) {
        return item instanceof ItemShardPouch || item instanceof CompassItem || item instanceof AntiqueStampCollectorBook || item instanceof FilterNecklaceItem;
    }
}

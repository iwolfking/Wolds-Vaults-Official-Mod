package xyz.iwolfking.woldsvaults.recipes.gear;

import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModItems;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.world.data.PlayerVaultStatsData;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.util.WoldGearModifierHelper;

import java.util.*;

public class AdaptiveFocusUniqueGearRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultGearItem && secondary.getItem() == ModItems.ADAPTIVE_FOCUS && context.getPlayer().isPresent()) {
            ItemStack output = primary.copy();
            VaultGearData gear = VaultGearData.read(output);

            if(!gear.getRarity().equals(VaultGearRarity.UNIQUE)) {
                return false;
            }

            Player player = context.getPlayer().get();

            if(player instanceof ServerPlayer serverPlayer) {
                int playerLevel = PlayerVaultStatsData.get(serverPlayer.getLevel()).getVaultStats(serverPlayer).getVaultLevel();
                if(!WoldGearModifierHelper.reforgeModifiersForNewLevelForced(output, Math.min(100, playerLevel), new Random(), true).success()) {
                    return false;
                }
            }
            else {
                if(WoldGearModifierHelper.reforgeModifiersForNewLevelForced(output, Math.min(100, VaultBarOverlay.vaultLevel), new Random(), true).success()) {
                    return false;
                }
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

        ItemStack swordInput = new ItemStack(iskallia.vault.init.ModItems.SWORD);
        VaultGearData gearData = VaultGearData.read(swordInput);

        gearData.setRarity(VaultGearRarity.UNIQUE);
        gearData.write(swordInput);

        ItemStack secondaryInput = new ItemStack(ModItems.ADAPTIVE_FOCUS);

        ItemStack swordOutput = swordInput.copy();
        WoldGearModifierHelper.reforgeModifiersForNewLevelForced(swordOutput, 100, new Random(), true);

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(swordInput, List.of(secondaryInput), List.of(swordOutput))));
    }
}

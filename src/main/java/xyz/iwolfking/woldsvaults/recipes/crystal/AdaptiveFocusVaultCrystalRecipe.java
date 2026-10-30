package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.world.data.PlayerVaultStatsData;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class AdaptiveFocusVaultCrystalRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultCrystalItem && secondary.getItem() == ModItems.ADAPTIVE_FOCUS && context.getPlayer().isPresent()) {
            ItemStack output = primary.copy();
            CrystalData crystal = CrystalData.read(output);

            if(crystal.getProperties().isUnmodifiable()) {
                return false;
            }

            Player player = context.getPlayer().get();

            if(player instanceof ServerPlayer serverPlayer) {
                int playerLevel = PlayerVaultStatsData.get(serverPlayer.getLevel()).getVaultStats(serverPlayer).getVaultLevel();
                if(playerLevel <= crystal.getProperties().getLevel().orElse(0)) {
                    return false;
                }
                crystal.getProperties().setLevel(playerLevel);
                crystal.write(output);
            }
            else {
                if(VaultBarOverlay.vaultLevel <= crystal.getProperties().getLevel().orElse(0)) {
                    return false;
                }

                crystal.getProperties().setLevel(VaultBarOverlay.vaultLevel);

                crystal.write(output);
            }

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

        ItemStack crystalInput = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData data = CrystalData.read(crystalInput);
        data.getProperties().setLevel(0);
        data.write(crystalInput);

        ItemStack secondaryInput = new ItemStack(ModItems.ADAPTIVE_FOCUS);

        ItemStack crystalOutput = crystalInput.copy();
        CrystalData dataOutput = CrystalData.read(crystalOutput);
        dataOutput.getProperties().setLevel(100);
        dataOutput.write(crystalOutput);


        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(crystalInput, List.of(secondaryInput), List.of(crystalOutput))));
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.ItemVaultCrystalSeal;
import iskallia.vault.recipe.anvil.AnvilExecutor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.vhapi.mixin.accessors.VaultCrystalConfigAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.SealEntryAccessor;
import xyz.iwolfking.woldsvaults.objectives.lib.ScalingObjective;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(value = AnvilExecutor.class, remap = false)
public abstract class MixinAnvilExecutor {
    @Shadow
    private static void applyIngredient(Player player, AnvilExecutor.Result result, int index, ItemStack ingredient, BiConsumer<Integer, ItemStack> setIngredientResult, boolean setUsedSlot, boolean uniqueIngredient) {
    }

    /**
     * @author iwolfking
     * @reason Wold's modifications of how crystal workbench tile entity applies things
     */
    @Overwrite
    public static AnvilExecutor.Result test(Player player, ItemStack input, List<ItemStack> ingredients, List<ItemStack> uniqueIngredients) {
        AnvilExecutor.Result result = new AnvilExecutor.Result(ingredients.size(), uniqueIngredients.size());
        result.setOutput(input.copy());

        for (int index = 0; index < uniqueIngredients.size(); ++index) {
            if (index != 2 && index != 5) {
                ItemStack uniqueIngredient = uniqueIngredients.get(index);
                if(uniqueIngredient != null) {
                    applyIngredient(player, result, index, uniqueIngredient, result::setUniqueIngredientResult, false, true);
                    applyScalingSeals(player, result.getUniqueIngredient(index), result, index);
                }
            }
        }

        for (int index = 0; index < ingredients.size(); ++index) {
            ItemStack leftIngredient = ingredients.get(index).copy();
            if(leftIngredient != null) {
                applyIngredient(player, result, index, leftIngredient, result::setIngredientResult, true, false);
            }
        }

        ItemStack capstone = uniqueIngredients.get(2);
        if(capstone != null) {
            applyIngredient(player, result, 2, capstone, result::setUniqueIngredientResult, false, true);
        }

        ItemStack map = uniqueIngredients.get(5);
        if(map != null) {
            applyIngredient(player, result, 5, map, result::setUniqueIngredientResult, false, true);
        }

        return result;
    }

    private static boolean applyScalingSeals(Player player, ItemStack uniqueIngredient, AnvilExecutor.Result result, int index) {
        if (uniqueIngredient == null) {
            return false;
        }
        var sealCfg = uniqueIngredient.getItem() instanceof ItemVaultCrystalSeal seal ? ((VaultCrystalConfigAccessor)ModConfigs.VAULT_CRYSTAL).getSeals().get(seal.getRegistryName()) : null;
        boolean applied = false;
        if (sealCfg != null) {
            if (((SealEntryAccessor)sealCfg.get(0)).getObjective() instanceof ScalingObjective scalingObjective) {
                for (var i = 0; i < scalingObjective.getMaxSealCount(); i++) {
                    applyIngredient(player, result, index, uniqueIngredient, result::setUniqueIngredientResult, false, true);
                    uniqueIngredient = result.getUniqueIngredient(index);
                    applied = true;
                    if (uniqueIngredient == null) break;
                }
            };
        }
        return applied;
    }
}

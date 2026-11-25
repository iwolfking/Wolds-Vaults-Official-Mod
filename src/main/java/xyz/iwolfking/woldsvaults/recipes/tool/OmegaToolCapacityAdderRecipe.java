package xyz.iwolfking.woldsvaults.recipes.tool;

import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import iskallia.vault.world.data.PlayerVaultStatsData;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;

public class OmegaToolCapacityAdderRecipe extends VanillaAnvilRecipe {

    private static final String REINFORCEMENT_COUNT_TAG = "woldsvaults.crystal_reinforcements_applied";
    private static final int CAPACITY_PER_REINFORCEMENT = 1;
    private static final int BASE_MAX_REINFORCEMENTS = 5;
    private static final int LEVEL_THRESHOLD = 100;
    private static final int LEVEL_STEP = 25;

    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];

        if (primary.getItem() instanceof ToolItem && secondary.getItem() == ModItems.CRYSTAL_REINFORCEMENT) {
            ItemStack output = primary.copy();
            VaultGearData gear = ToolGearData.read(output);

            if (gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).isEmpty()) {
                return false;
            }

            int toolCapacity = gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).get();

            CompoundTag tag = output.getOrCreateTag();
            int reinforcementsApplied = tag.contains(REINFORCEMENT_COUNT_TAG)
                    ? tag.getInt(REINFORCEMENT_COUNT_TAG)
                    : 0;

            int maxReinforcements = getMaxReinforcementsForLevel(getPlayerLevel(context));
            if (reinforcementsApplied >= maxReinforcements) {
                return false;
            }

            gear.createOrReplaceAttributeValue(ModGearAttributes.TOOL_CAPACITY, toolCapacity + CAPACITY_PER_REINFORCEMENT);
            gear.write(output);

            tag.putInt(REINFORCEMENT_COUNT_TAG, reinforcementsApplied + 1);

            context.setOutput(output);

            context.onTake(context.getTake().append(() -> {
                context.getInput()[0].shrink(1);
                context.getInput()[1].shrink(1);
            }));
            return true;
        }

        return false;
    }

    private int getMaxReinforcementsForLevel(int level) {
        if (level <= LEVEL_THRESHOLD) {
            return BASE_MAX_REINFORCEMENTS;
        }
        int extraSlots = (level - LEVEL_THRESHOLD) / LEVEL_STEP;
        return BASE_MAX_REINFORCEMENTS + extraSlots;
    }

    private int getPlayerLevel(AnvilContext context) {
        Player player = context.getPlayer().orElse(null);
        if (player == null) {
            return 0;
        }

        if(player instanceof ServerPlayer serverPlayer) {
            return PlayerVaultStatsData.get(serverPlayer.getServer()).getVaultStats(player).getVaultLevel();
        }
        else {
           return VaultBarOverlay.VAULT_LEVEL_SUPPLIER.get();
        }

    }

    @Override
    public void onRegisterJEI(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        List<ItemStack> secondary = List.of(new ItemStack(ModItems.CRYSTAL_REINFORCEMENT));

        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolType type : ToolType.values()) {
                inputs.add(ToolItem.create(material, type));
                ItemStack output = ToolItem.create(material, type);
                ToolItem.addCapacity(output, CAPACITY_PER_REINFORCEMENT);
                outputs.add(output);
            }
        }

        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(inputs, secondary, outputs)));
    }
}
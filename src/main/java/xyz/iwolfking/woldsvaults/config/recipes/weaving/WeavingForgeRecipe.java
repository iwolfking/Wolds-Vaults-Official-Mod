package xyz.iwolfking.woldsvaults.config.recipes.weaving;

import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.gear.crafting.recipe.TrinketForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.research.StageManager;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import xyz.iwolfking.woldsvaults.config.TrinketPouchConfig;
import xyz.iwolfking.woldsvaults.data.discovery.ClientRecipeDiscoveryData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.TargetedModBox;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;

import java.util.List;

public class WeavingForgeRecipe extends VaultForgeRecipe {

    public WeavingForgeRecipe(ResourceLocation id, ItemStack output, List<ItemStack> inputs) {
        super(ForgeRecipeType.valueOf("WEAVING"), id, output, inputs);
    }

    public WeavingForgeRecipe(Object o, Object o1) {
        super(ForgeRecipeType.valueOf("WEAVING"), (ResourceLocation) o, (ItemStack) o1);
    }

    @Override
    public ItemStack createOutput(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel) {
        return super.createOutput(consumed, crafter, vaultLevel);
    }

    @Override
    public void addCraftingDisplayTooltip(ItemStack result, List<Component> out) {
        if(result.getItem() instanceof TrinketPouchItem) {
            TrinketPouchConfig.TrinketPouchConfigEntry entry = TrinketPouchItem.getPouchConfigFor(result);
            for(String key : entry.SLOT_ENTRIES.keySet()) {
                out.add(getSlotsText(entry, key).copy().withStyle(ChatFormatting.BLUE));
            }
        }
    }

    private static Component getTranslatedTrinketName(String key) {
        return new TranslatableComponent("curios.identifier." + key);
    }

    private static Component getSlotsText(TrinketPouchConfig.TrinketPouchConfigEntry entry, String key) {
        return new TranslatableComponent("item.woldsvaults.trinket_pouch_slot_count", entry.SLOT_ENTRIES.get(key), getTranslatedTrinketName(key));
    }

    @Override
    public boolean canCraft(Player player, int level) {
        if(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.containsKey(this.getId())) {
            if (player instanceof ServerPlayer sPlayer) {
                return player.isCreative() || DiscoveredRecipesData.get(sPlayer.server).hasDiscovered(player, this.getId());
            }

            return player.isCreative() || ClientRecipeDiscoveryData.getDiscoveredRecipes().contains(this.getId());
        }

        return true;
    }

    @Override
    public List<Component> getDisabledText() {
        if(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.containsKey(this.getId())) {
            return List.of(new TextComponent(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.get(this.getId()).DESCRIPTION));
        }
        return super.getDisabledText();
    }
}

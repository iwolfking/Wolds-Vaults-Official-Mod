package xyz.iwolfking.woldsvaults.config.recipes.mod_box;

import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.item.AugmentItem;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.StageManager;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.data.discovery.ClientThemeDiscoveryData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredThemesData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.TargetedModBox;

import java.util.List;
import java.util.Optional;

public class ModBoxForgeRecipe extends VaultForgeRecipe {

    public ModBoxForgeRecipe(ResourceLocation id, ItemStack output, List<ItemStack> inputs) {
        super(ForgeRecipeType.valueOf("MOD_BOX"), id, output, inputs);
    }

    public ModBoxForgeRecipe(Object o, Object o1) {
        super(ForgeRecipeType.valueOf("MOD_BOX"), (ResourceLocation) o, (ItemStack) o1);
    }

    @Override
    public ItemStack createOutput(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel) {
        return super.createOutput(consumed, crafter, vaultLevel);
    }

    @Override
    public void addCraftingDisplayTooltip(ItemStack result, List<Component> out) {
        String research = TargetedModBox.getResearchTag(result);
        out.add(new TextComponent("Research: ").append(new TextComponent(research).withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public boolean canCraft(Player player) {
        String research = this.output.getOrCreateTag().getString("research");
        if (player instanceof ServerPlayer sPlayer) {
            PlayerResearchesData data = PlayerResearchesData.get(sPlayer.getLevel());
            return data.getResearches(player).isResearched(research);
        }

        return StageManager.getResearchTree(player).isResearched(research);
    }
}

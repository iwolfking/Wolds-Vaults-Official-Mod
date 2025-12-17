package xyz.iwolfking.woldsvaults.config.recipes.augment;

import iskallia.vault.config.entry.DescriptionData;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.AugmentItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.data.discovery.ClientThemeDiscoveryData;
import xyz.iwolfking.woldsvaults.api.data.discovery.DiscoveredThemesData;

import java.util.List;

public class AugmentForgeRecipe extends VaultForgeRecipe {

    public AugmentForgeRecipe(ResourceLocation id, ItemStack output, List<ItemStack> inputs) {
        super(ForgeRecipeType.valueOf("AUGMENT"), id, output, inputs);
    }

    public AugmentForgeRecipe(Object o, Object o1) {
        super(ForgeRecipeType.valueOf("AUGMENT"), (ResourceLocation) o, (ItemStack) o1);
    }

    @Override
    public ItemStack createOutput(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel) {
        return super.createOutput(consumed, crafter, vaultLevel);
    }

    @Override
    public void addCraftingDisplayTooltip(ItemStack result, List<Component> out) {
        AugmentItem.getTheme(result).ifPresent((key) -> ModConfigs.THEME_AUGMENT_LORE.getAugmentLore(key.getId()).ifPresentOrElse((lore) -> {
            MutableComponent var10001 = (new TextComponent("Theme: ")).withStyle(ChatFormatting.GRAY);
            String var10004 = lore.displayName;
            out.add(var10001.append((new TextComponent(var10004 + " | " + key.getName())).withStyle(ChatFormatting.WHITE)));

            for(DescriptionData data : lore.description) {
                out.add(data.getComponent());
            }

        }, () -> out.add((new TextComponent("Theme: ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(key.getName())).withStyle(ChatFormatting.WHITE)))));
    }

    @Override
    public boolean canCraft(Player player, int level) {
        if (player instanceof ServerPlayer sPlayer) {
            DiscoveredThemesData themesData = DiscoveredThemesData.get(sPlayer.getLevel());
            return themesData.hasDiscovered(sPlayer, this.getId());
        } else {
            return ClientThemeDiscoveryData.getDiscoveredThemes().contains(this.getId());
        }
    }
}

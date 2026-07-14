package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Extremely rare omega-injection drop from hyperboss completion crates. Inert on its own;
 * surrounded by 8 Blocks of Omega Pog it crafts into God's Mastery.
 */
public class CoreOfTheVaultGodsItem extends BasicItem {
    public CoreOfTheVaultGodsItem(ResourceLocation id) {
        super(id, new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS).rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(new TextComponent("An inert sphere cut from the heart of the Vault.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Four faint glows stir beneath its shell.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Surround it with Blocks of Omega Pog to awaken it.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}

package xyz.iwolfking.woldsvaults.items;

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

/** An armory upgrade: placed in an armory, it widens what a Load applies. */
public class ArmoryUpgradeItem extends Item {
    private final String text;

    public ArmoryUpgradeItem(ResourceLocation id, String text) {
        super(new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS).stacksTo(1).rarity(Rarity.RARE));
        this.text = text;
        this.setRegistryName(id);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("Armory upgrade").withStyle(ChatFormatting.GOLD));
        tooltip.add(new TextComponent(this.text).withStyle(ChatFormatting.GRAY));
    }
}

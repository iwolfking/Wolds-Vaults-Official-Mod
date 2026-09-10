package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.entry.DescriptionData;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.AugmentItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.util.WoldTexFX;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;
import java.util.Optional;

public class InfusedAugmentItem extends AugmentItem {

    public InfusedAugmentItem(CreativeModeTab group, ResourceLocation id) {
        super(group, id);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        getTheme(stack).ifPresent((key) -> ModConfigs.THEME_AUGMENT_LORE.getAugmentLore(key.getId()).ifPresentOrElse((lore) -> {
            MutableComponent var10001 = (new TextComponent("Theme: ")).withStyle(ChatFormatting.GRAY);
            String var10004 = lore.displayName;
            tooltip.add(var10001.append((WoldTexFX.rainbowEffect(new TextComponent(var10004 + " | " + key.getName())))));

            for(DescriptionData data : lore.description) {
                tooltip.add(data.getComponent());
            }

        }, () -> tooltip.add((new TextComponent("Theme: ")).withStyle(ChatFormatting.GRAY).append((WoldTexFX.rainbowEffect(new TextComponent(key.getName())))))));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (this.allowdedIn(category)) {
            for(ThemeKey key : VaultRegistry.THEME.getKeys()) {
                ItemStack stack = new ItemStack(ModItems.INFUSED_AUGMENT);
                stack.getOrCreateTag().putString("theme", key.getId().toString());
                items.add(stack);
            }

        }
    }

    public static ItemStack create(ResourceLocation theme) {
        ItemStack stack = new ItemStack(ModItems.INFUSED_AUGMENT);
        stack.getOrCreateTag().putString("theme", theme.toString());
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
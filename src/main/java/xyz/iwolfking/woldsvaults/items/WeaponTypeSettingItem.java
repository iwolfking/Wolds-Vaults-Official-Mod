package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.config.WeaponTypesConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;
import java.util.Map;

public class WeaponTypeSettingItem extends Item {
    public WeaponTypeSettingItem(ResourceLocation id, Properties properties) {
        super(properties);
        this.setRegistryName(id);
    }

    public @NotNull String getModifierTagString(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof WeaponTypeSettingItem) {
           return stack.getOrCreateTag().getString("modifier");
        }
        return "";
    }

    public String displayAllowedValues(String type) {
        WeaponTypesConfig.Entry entry = ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.get(type);
        return entry == null ? "" : entry.displayAllowedTypes();
    }

    public static ItemStack create(String tag) {
        ItemStack stack = new ItemStack(ModItems.WEAPON_TYPE_SETTER);
        stack.getOrCreateTag().putString("modifier", tag);
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        String display = getModifierTagString(stack);
        MutableComponent text = (new TextComponent("Combine with a Vault Weapon in an Anvil to set the Weapon Type to ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(display).append(new TextComponent(".").withStyle(ChatFormatting.GRAY))));
        tooltip.add(text);
        WeaponTypesConfig.Entry entry = ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.get(display);
        if (entry != null && entry.ALLOWED_TYPES != null && !entry.ALLOWED_TYPES.isEmpty()) {
            MutableComponent allowed = new TextComponent("Allowed On: ").withStyle(ChatFormatting.GRAY);
            boolean first = true;
            for (String type : entry.ALLOWED_TYPES) {
                if (!first) {
                    allowed.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));
                }
                first = false;
                allowed.append(new TextComponent(type).withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(allowed);
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String weaponType = getModifierTagString(stack);
        return new TextComponent("Weapon Augmenter").append(" - " + weaponType);
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(ModCreativeTabs.WOLDS_VAULTS)) {
            for (Map.Entry<String, WeaponTypesConfig.Entry> weaponType : ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.entrySet()) {
                items.add(create(weaponType.getKey()));
            }
        }
    }
}
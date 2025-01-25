package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.gear.DataInitializationItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.modifiers.vault.readers.WeaponTypeReader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class WeaponTypeSettingItem extends BasicItem{


    public WeaponTypeSettingItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    public @NotNull String getModifierTagString(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof WeaponTypeSettingItem) {
           return stack.getOrCreateTag().getString("modifier");
        }
        return "";
    }

    public String displayAllowedValues(String type) {
        return ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.get(type).displayAllowedTypes();
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
        tooltip.add(new TextComponent(displayAllowedValues(getModifierTagString(stack))));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String weaponType = getModifierTagString(stack);
        return new TextComponent("Weapon Augmenter").append(" - " + weaponType);
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            //items.add(create("Sword"));
        }
    }
}

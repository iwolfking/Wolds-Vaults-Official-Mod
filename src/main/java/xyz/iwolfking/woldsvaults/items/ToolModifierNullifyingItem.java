package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.item.BasicItem;
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
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class ToolModifierNullifyingItem extends BasicItem {

    public static List<String> CHISELING_MODIFIER_TYPES = List.of("copiously", "reaping", "picking", "axing", "shovelling", "ornate_affinity", "coin_affinity", "gilded_affinity", "living_affinity", "wooden_affinity", "hammering", "hydrovoid", "rotating", "durability", "immortality", "mining_speed", "item_quantity", "item_rarity", "trap_disarming", "soulbound", "smelting", "pulverizing");


    public ToolModifierNullifyingItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @Nullable
    public static VaultGearAttribute<?> getModifierTag(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ToolModifierNullifyingItem) {
            String tagStr = stack.getOrCreateTag().getString("modifier");
            return VaultGearAttributeRegistry.getAttribute(ResourceLocation.tryParse(tagStr));
        } else {
            return null;
        }
    }

    @Nullable
    public static String getModifierTagString(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ToolModifierNullifyingItem) {
            String tagStr = stack.getOrCreateTag().getString("modifier");
            VaultGearAttribute<?> attr = VaultGearAttributeRegistry.getAttribute(ResourceLocation.tryParse(tagStr));
            if(attr != null) {
                VaultGearModifierReader<?> reader = attr.getReader();
                return reader.getModifierName();
            }

        }

        return "";
    }

    public static ItemStack create(String tag) {
        ItemStack stack = new ItemStack(ModItems.CHISELING_FOCUS);
        stack.getOrCreateTag().putString("modifier", String.valueOf(VaultMod.id(tag)));
        return stack;
    }

    public static void setModifierTag(ItemStack stack, String tag) {
        if (!stack.isEmpty() && stack.getItem() instanceof ToolModifierNullifyingItem) {
            stack.getOrCreateTag().putString("modifier", tag);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
            VaultGearAttribute<?> attr = getModifierTag(stack);
            if (attr != null) {
                    VaultGearModifierReader<?> reader = attr.getReader();
                    MutableComponent text = (new TextComponent("Combine with a Vault Tool in an Anvil to remove ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(reader.getModifierName())).withStyle(reader.getColoredTextStyle()).append(new TextComponent(" from it.").withStyle(ChatFormatting.GRAY)));
                    tooltip.add(text);
            }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return new TextComponent("Chiseling Focus").append(" - " + getModifierTagString(stack));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            for(String modifierType : CHISELING_MODIFIER_TYPES) {
                items.add(create(modifierType));
            }
        }
    }
}

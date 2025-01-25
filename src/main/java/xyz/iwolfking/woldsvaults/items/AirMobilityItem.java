package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AirMobilityItem extends BasicItem implements ICurioItem {
    public AirMobilityItem(ResourceLocation id) {
        super(id);
    }

    Set<UUID> disabledVaultUUIDs = new HashSet<>();

    // modify base player attributes :3
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if(slotContext.entity().hasEffect(MobEffects.SLOW_FALLING) || slotContext.entity().hasEffect(ModEffects.GROUNDED)) {
            return;
        }

        slotContext.entity().setSpeed(0.2F);
        slotContext.entity().flyingSpeed = slotContext.entity().getSpeed() * 0.5F;
    }

    // allow for the curio item to be equipped via right-click
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    // append tooltip hover text
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        MutableComponent text = new TextComponent("Allows you to freely move in the air").withStyle(ChatFormatting.YELLOW);
        tooltip.add(text);
    }
}

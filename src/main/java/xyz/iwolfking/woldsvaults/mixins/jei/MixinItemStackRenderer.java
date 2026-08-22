package xyz.iwolfking.woldsvaults.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.common.render.ItemStackRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jei"),
                @Condition(type = Condition.Type.MOD, value = "sophisticatedbackpacks"),
                @Condition(type = Condition.Type.MOD, value = "sophisticatedstorage")
        }
)
@Mixin(value = ItemStackRenderer.class, remap = false)
public class MixinItemStackRenderer {
    @WrapOperation(method = "getTooltip(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"), remap = true)
    private List<Component> addSophSlotsAndUpgradesToTooltip(ItemStack stack, Player pPlayer, TooltipFlag pIsAdvanced, Operation<List<Component>> original){
        List<Component> tooltip = original.call(stack, pPlayer, pIsAdvanced);
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof StorageBlockBase storageBlockBase) {
                int slots = storageBlockBase.getNumberOfInventorySlots();
                int upgrades = storageBlockBase.getNumberOfUpgradeSlots();
                if (tooltip instanceof ArrayList<?>) { // mutable
                    tooltip.add(1, new TextComponent("□ " + slots + " | " + "↑ " + upgrades).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        if (stack.getItem() instanceof BackpackItem backpackItem) {
            int slots = backpackItem.getNumberOfSlots();
            int upgrades = backpackItem.getNumberOfUpgradeSlots();
            if (tooltip instanceof ArrayList<?>) { // mutable
                tooltip.add(1, new TextComponent("□ " + slots + " | " + "↑ " + upgrades).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return tooltip;
    }
}

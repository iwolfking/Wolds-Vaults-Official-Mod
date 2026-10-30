package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.items.fruits.FruitTooltips;

import java.util.List;
import java.util.UUID;

@Mixin(value = ItemVaultFruit.class, remap = false)
public class MixinItemVaultFruitClient {

    @ModifyArg(method = "appendHoverText",
               at = @At(value = "INVOKE",
                        target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                        ordinal = 1),
            remap = true
               )
    public Object changeHPNumber(Object cmp) {
        var ttip = FruitTooltips.getHPTooltip();
        if (ttip == null) {
            return cmp;
        }
        return ttip;
    }

    @Inject(method = "appendHoverText",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 2,
                    shift = At.Shift.AFTER),
            remap = true
            )
    public void fruitCount(ItemStack itemStack, Level worldIn, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        var ttip = FruitTooltips.getFruitCountTooltip();
        if (ttip != null) {
            tooltip.add(ttip);
        }
    }
}

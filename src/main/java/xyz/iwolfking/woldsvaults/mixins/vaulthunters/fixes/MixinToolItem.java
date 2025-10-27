package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.item.tool.ToolItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.justahuman.vaultlootbeams.mixin.colors.vault.ToolItemMixin;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vaultlootbeams")
    }
)
@Mixin(value = ToolItem.class, remap = false, priority = 1500)
public class MixinToolItem {

    @SuppressWarnings("target")
    @Dynamic(value = "getBeamColor should be injected by VaultLootBeams at runtime", mixin = ToolItemMixin.class)
    @Inject(method = "getBeamColor(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;F)Ljava/awt/Color;", at = @At("HEAD"), cancellable = true)
    private void nulliteLootBeamColor(ItemEntity entity, ItemStack itemStack, float partialTicks, CallbackInfoReturnable<Color> cir) {
        if (ToolItem.getMaterial(itemStack).name().equals("NULLITE")) {
            cir.setReturnValue(new Color(29, 15, 43));
        }
    }
}

package xyz.iwolfking.woldsvaults.mixins.sophisticatedstorage;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "sophisticatedstorage")
    }
)
@Mixin(value = WoodStorageBlockBase.class)
public class MixinPackingTape {
    @Redirect(method = "packStorage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
    private static void removeDurabilityFromPackingTape(ItemStack instance, int pDamage) {
        
    }

}

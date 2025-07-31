package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import cofh.ensorcellation.init.EnsorcEnchantments;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpiritExtractorTileEntity.RecoveryCost.class, remap = false)
public class MixinSpiritExtractorTileEntity {
    @Inject(method = "isSoulbound", at = @At("HEAD"), cancellable = true)
    private void returnSoulboundEnchantAsSoulbound(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(EnchantmentHelper.getEnchantments(stack).containsKey(EnsorcEnchantments.SOULBOUND.get())) {
            cir.setReturnValue(true);
        }
    }
}

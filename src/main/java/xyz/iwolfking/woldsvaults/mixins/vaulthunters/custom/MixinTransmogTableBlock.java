package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.TransmogTableBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransmogTableBlock.class, remap = false)
public class MixinTransmogTableBlock {
    @Inject(method = "checkModelAccess", at = @At("HEAD"), cancellable = true)
    private static void dontCheckRequirementsForTransmogs(Player player, ResourceLocation modelId, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(null);
    }
}

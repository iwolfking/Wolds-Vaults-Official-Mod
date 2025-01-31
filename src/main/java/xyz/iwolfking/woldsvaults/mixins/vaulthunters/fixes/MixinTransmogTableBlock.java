package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.TransmogTableBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Mixin Class currently used to ignore the patreon check when checking in the transmog table
 */
@Mixin(value = TransmogTableBlock.class, remap = false)
public class MixinTransmogTableBlock {
    @Inject(method = "canTransmogModel", at = @At("HEAD"), cancellable = true)
    private static void preventPatreonCheck(Player player, Collection<ResourceLocation> discoveredModelIds, ResourceLocation modelId, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(discoveredModelIds.contains(modelId));
    }
}

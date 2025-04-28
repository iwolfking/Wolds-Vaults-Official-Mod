package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.data.InscriptionData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InscriptionData.class, remap = false)
public class MixinInscriptionData {
    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultModifierPoolsConfig;getRandom(Lnet/minecraft/resources/ResourceLocation;ILiskallia/vault/core/random/RandomSource;)Ljava/util/List;", ordinal = 0), cancellable = true)
    private void cancelInsteadOfCursing(Player player, ItemStack stack, CrystalData crystal, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}

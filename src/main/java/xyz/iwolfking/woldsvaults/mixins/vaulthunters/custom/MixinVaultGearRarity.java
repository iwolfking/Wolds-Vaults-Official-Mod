package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

@Mixin(value = VaultGearRarity.class, remap = false)
public abstract class MixinVaultGearRarity {
    /**
     * @author iwolfking
     * @reason Fix Sacred and Mythic and prevent them from having Pyretic focus used on them.
     */
    @Inject(method = "getNextTier", at = @At("HEAD"), cancellable = true)
    public void getNextTier(CallbackInfoReturnable<VaultGearRarity> cir) {
        if(this.equals(VaultGearRarity.CHAOTIC)) {
            cir.setReturnValue(null);
        }
        if(this.equals(VaultGearRarity.valueOf("MYTHIC"))) {
            cir.setReturnValue(null);
        }
    }
}

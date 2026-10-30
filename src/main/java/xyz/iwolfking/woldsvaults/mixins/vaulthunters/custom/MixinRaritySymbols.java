package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.util.RaritySymbols;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RaritySymbols.class, remap = false)
public class MixinRaritySymbols {
    private static final String MYTHIC_SYMBOL = "✧";

    @Inject(method = "getSymbol(Liskallia/vault/gear/VaultGearRarity;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void injectMythicSymbol(VaultGearRarity rarity, CallbackInfoReturnable<String> cir) {
        if (rarity.name().equals("MYTHIC")) {
            cir.setReturnValue(MYTHIC_SYMBOL);
        }
    }

    @Inject(method = "getRedGreenBlindColor(Liskallia/vault/gear/VaultGearRarity;)Lnet/minecraft/network/chat/TextColor;", at = @At("HEAD"), cancellable = true)
    private static void injectRedGreenBlindColor(VaultGearRarity rarity, CallbackInfoReturnable<TextColor> cir) {
        if (rarity.name().equals("MYTHIC")) {
            cir.setReturnValue(TextColor.fromRgb(8069026));
        }
    }

    @Inject(method = "getBlueYellowBlindColor(Liskallia/vault/gear/VaultGearRarity;)Lnet/minecraft/network/chat/TextColor;", at = @At("HEAD"), cancellable = true)
    private static void injectBlueYellowBlindColor(VaultGearRarity rarity, CallbackInfoReturnable<TextColor> cir) {
        if (rarity.name().equals("MYTHIC")) {
            cir.setReturnValue(TextColor.fromRgb(12000284));
        }
    }
}

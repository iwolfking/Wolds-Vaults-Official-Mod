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
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

@Mixin(value = VaultGearRarity.class, remap = false)
public abstract class MixinVaultGearRarityColor {
//    @Shadow @Final private TextColor color;
//
//    @Shadow @Final public static VaultGearRarity SCRAPPY;
//
//    /**
//     * @author iwolfking
//     * @reason Replaces how vault gear rarity colors are determined.
//     */
//    @Overwrite
//    public TextColor getColor() {
//        if(this.equals(VaultGearRarity.valueOf("MYTHIC"))) {
//            return WoldsVaultsConfig.CLIENT.mythicGearColor.get();
//        }
//
//        return this.color;
//    }
}

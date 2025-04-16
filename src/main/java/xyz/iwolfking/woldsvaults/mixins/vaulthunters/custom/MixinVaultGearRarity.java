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
    @Shadow @Final private TextColor color;

    /**
     * @author iwolfking
     * @reason Replaces how vault gear rarity colors are determined.
     */
    @Overwrite
    public TextColor getColor() {
        if(ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.GEAR_RARITY_COLOR_MAP.containsKey(this)) {
                TextColor color = ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.GEAR_RARITY_COLOR_MAP.get(this);
                return color != null ? color : TextColor.fromLegacyFormat(ChatFormatting.WHITE);
        }
        return this.color;
    }

    /**
     * @author iwolfking
     * @reason Fix Sacred and Mythic and prevent them from having Pyretic focus used on them.
     */
    @Inject(method = "getNextTier", at = @At("HEAD"), cancellable = true)
    public void getNextTier(CallbackInfoReturnable<VaultGearRarity> cir) {
        if(this.equals(VaultGearRarity.valueOf("MYTHIC"))) {
            cir.setReturnValue(null);
        }

        if(this.equals(VaultGearRarity.valueOf("SACRED"))) {
            cir.setReturnValue(null);
        }
    }
}

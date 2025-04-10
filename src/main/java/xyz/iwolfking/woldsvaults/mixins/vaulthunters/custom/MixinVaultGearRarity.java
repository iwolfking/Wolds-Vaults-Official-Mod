package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

@Mixin(value = VaultGearRarity.class, remap = false)
public class MixinVaultGearRarity {
    @Shadow @Final private TextColor color;

    /**
     * @author iwolfking
     * @reason Test
     */
    @Overwrite
    public TextColor getColor() {
        if(ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.GEAR_RARITY_COLOR_MAP.containsKey(this)) {
                TextColor color = ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.GEAR_RARITY_COLOR_MAP.get(this);
                return color != null ? color : TextColor.fromLegacyFormat(ChatFormatting.WHITE);
        }
        return this.color;
    }
}

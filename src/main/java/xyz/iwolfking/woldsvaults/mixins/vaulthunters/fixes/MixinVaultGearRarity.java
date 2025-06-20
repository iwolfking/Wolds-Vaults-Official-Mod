package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.StringUtils;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = VaultGearRarity.class, remap = false)
public class MixinVaultGearRarity {
    @Shadow @Final public static VaultGearRarity SCRAPPY;
    @Shadow @Final public static VaultGearRarity COMMON;
    @Shadow @Final public static VaultGearRarity RARE;
    @Shadow @Final public static VaultGearRarity EPIC;
    @Shadow @Final public static VaultGearRarity OMEGA;
    @Shadow @Final public static VaultGearRarity UNIQUE;
    @Shadow @Final private TextColor color;

    /**
     * @author iwolfking
     * @reason Add missing Mythic rarity handling.
     */
    @Overwrite
    public TextColor getColor() {
        try {
            if (ModConfigs.VAULT_GEAR_TYPE_CONFIG != null) {
                String var10000;
                if (this.equals(COMMON)) {
                    var10000 = "Common";
                } else if (this.equals(RARE)) {
                    var10000 = "Rare";
                } else if (this.equals(EPIC)) {
                    var10000 = "Epic";
                } else if (this.equals(OMEGA)) {
                    var10000 = "Omega";
                } else if (this.equals(UNIQUE)) {
                    var10000 = "Unique";
                }
                else if(this.equals(VaultGearRarity.valueOf("MYTHIC"))) {
                    var10000 = "Mythic";
                } else {
                    var10000 = "Scrappy";
                }

                String rollType = var10000;
                return (TextColor)ModConfigs.VAULT_GEAR_TYPE_CONFIG.getRollPool(rollType).map((pool) -> TextColor.fromRgb(pool.getColor())).orElse(this.color);
            } else {
                return this.color;
            }
        } catch (Exception var2) {
            return this.color;
        }
    }
}

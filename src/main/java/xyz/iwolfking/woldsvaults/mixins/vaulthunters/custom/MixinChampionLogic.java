package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.entity.champion.ChampionLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.modifiers.vault.ChampionDropsVaultModifier;

import java.util.Random;

@Mixin(value = ChampionLogic.class, remap = false)
public class MixinChampionLogic {

    @ModifyVariable(method = "lambda$onEntityDrops$4", at = @At(value = "STORE"), ordinal = 1)
    private static int modifyRolls(int value, @Local(argsOnly = true) Vault vault) {
        float boostedRate = 0F;
        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifier instanceof ChampionDropsVaultModifier championDropsVaultModifier) {
                boostedRate += championDropsVaultModifier.properties().getAddend();
            }
        }

        Random random = new Random();

        while(boostedRate != 0F && value != 0) {
            if(boostedRate < 0F) {
                if(boostedRate <= -1F) {
                    boostedRate += 1F;
                    value = Math.max(0, value - 1);
                }
                else {
                    if(random.nextFloat() <= Math.abs(boostedRate)) {
                        boostedRate = 0F;
                        value = Math.max(0, value - 1);
                    }
                }
            }
            else {
                if(boostedRate >= 1F) {
                    boostedRate -= 1F;
                    value += 1;
                }
                else {
                    if(random.nextFloat() <= boostedRate) {
                        boostedRate = 0F;
                        value += 1;
                    }
                }
            }
        }

        return value;
    }

}

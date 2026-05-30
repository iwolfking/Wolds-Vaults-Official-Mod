package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.config.greed.GreedTrialsScreenConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.RoyaleVaultCache;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = VaultMobsConfig.class, remap = false)
public class MixinVaultMobsConfig {

    @WrapOperation(
        method = "scale", 
        at = @At(value = "INVOKE", target = "Liskallia/vault/config/greed/GreedTrialsScreenConfig;getGreedTierDifficultyIncrement()D")
    )
    private static double dontScaleRoyaleVaults(GreedTrialsScreenConfig instance, Operation<Double> original, @Local(argsOnly = true) LivingEntity entity) {
        Level level = entity.getLevel();

        Optional<Vault> vaultOpt = VaultUtils.getVault(level);
        if (vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            UUID vaultId = vault.get(Vault.ID);

            boolean isRoyale = RoyaleVaultCache.isRoyale(vaultId, () -> VaultUtils.isRoyaleVault(vault));
                
            if (isRoyale) {
                    return 0.0;
            }
        }

        return original.call(instance);
    }
}
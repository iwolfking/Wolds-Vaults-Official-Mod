package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.config.greed.GreedTrialsScreenConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.api.util.RoyaleVaultCache;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = VaultMobsConfig.class, remap = false)
public class MixinVaultMobsConfig {

    @WrapOperation(
        method = "scale", 
        at = @At(value = "INVOKE", target = "Liskallia/vault/config/greed/GreedTrialsScreenConfig;getGreedTierDifficultyIncrement()D")
    )
    private static double dontScaleRoyaleVaults(GreedTrialsScreenConfig instance, Operation<Double> original, @Local(argsOnly = true) LivingEntity entity, @Local(name = "difficulty") VaultDifficulty difficulty) {
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

        if(GameruleHelper.isEnabled(ModGameRules.GREED_SCALES_WITH_DIFFICULTY, entity.getLevel())) {
            return woldsVaults$getGreedScalePerDifficulty(difficulty);
        }

        return original.call(instance);
    }

    @Unique
    private static double woldsVaults$getGreedScalePerDifficulty(VaultDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 0.25;
            case NORMAL -> 0.35;
            case HARD -> 0.5;
            case IMPOSSIBLE -> 1.0;
            case FRAGGED -> 2.0;
            case PIECE_OF_CAKE -> 0.1;
        };
    }


}
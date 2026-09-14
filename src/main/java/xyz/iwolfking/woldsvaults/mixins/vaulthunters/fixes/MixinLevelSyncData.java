package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.LevelSyncData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LevelSyncData.class, remap = false)
public class MixinLevelSyncData {
    @WrapOperation(method = "getPlayerLevelCap", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/PlayerVaultStats;getLevelCap(I)I"))
    private int fixLevelCapMaybePossibly(int greedTier, Operation<Integer> original) {
        if(greedTier == 0) {
            return ModConfigs.LEVELS_META.getMaxLevel();
        }

        return original.call(greedTier);
    }
}

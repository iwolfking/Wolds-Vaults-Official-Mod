package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.entity.entity.VaultStormEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VaultStormEntity.class, remap = false)
public class MixinVaultStormEntity {
    /**
     * The storm bolt's ability power roll. {@code nextFloat(min, max)} returns the raw 0-1 roll once the minimum
     * (raised by Lightning Damage) reaches the maximum, so each bolt dealt 0-100% of ability power; such bolts
     * now deal the mean of the two bounds.
     */
    @WrapOperation(method = "lambda$tick$4", at = @At(value = "INVOKE", target = "Liskallia/vault/core/random/JavaRandom;nextFloat(FF)F"))
    private float woldsvaults$stormBoltRoll(JavaRandom random, float min, float max, Operation<Float> original) {
        if (min >= max) {
            return (min + max) / 2.0F;
        }
        return original.call(random, min, max);
    }
}

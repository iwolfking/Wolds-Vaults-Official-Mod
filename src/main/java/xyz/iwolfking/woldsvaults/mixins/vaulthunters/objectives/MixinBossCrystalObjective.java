package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.item.crystal.objective.BossCrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

import java.util.function.IntSupplier;

@Mixin(value = BossCrystalObjective.class, remap = false)
public class MixinBossCrystalObjective {
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$beginDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.begin(ObjectiveDifficulty.multiplier(sigil, vault));
    }

    @Inject(method = "configure", at = @At("RETURN"))
    private void woldsvaults$endDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.end();
    }

    /** Obelisk count and the mobs per wave both rise by the boss exponent. */
    @WrapOperation(method = "lambda$configure$1", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/ObeliskObjective;of(ILjava/util/function/IntSupplier;F)Liskallia/vault/core/vault/objective/ObeliskObjective;"))
    private ObeliskObjective woldsvaults$scaleObelisks(int target, IntSupplier wave, float objectiveProbability, Operation<ObeliskObjective> original) {
        double factor = ObjectiveDifficulty.factor(ObjectiveDifficulty.current(), "boss");
        IntSupplier scaledWave = () -> ObjectiveDifficulty.scale(wave.getAsInt(), factor);
        return original.call(ObjectiveDifficulty.scale(target, factor), scaledWave, objectiveProbability);
    }
}

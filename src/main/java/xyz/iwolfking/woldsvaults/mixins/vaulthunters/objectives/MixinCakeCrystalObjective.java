package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.objective.CakeCrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

@Mixin(value = CakeCrystalObjective.class, remap = false)
public class MixinCakeCrystalObjective {
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$beginDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.begin(ObjectiveDifficulty.multiplier(sigil, vault));
    }

    @Inject(method = "configure", at = @At("RETURN"))
    private void woldsvaults$endDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.end();
    }

    /** Only a crystal with a cake target scales; the eat-every-cake form counts rooms and is left alone. */
    @ModifyArg(method = "lambda$configure$0", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/CakeObjective;of(ILnet/minecraft/resources/ResourceLocation;)Liskallia/vault/core/vault/objective/CakeObjective;"), index = 0)
    private int woldsvaults$scaleTarget(int target) {
        return ObjectiveDifficulty.scale(target, ObjectiveDifficulty.factor(ObjectiveDifficulty.current(), "cake"));
    }
}

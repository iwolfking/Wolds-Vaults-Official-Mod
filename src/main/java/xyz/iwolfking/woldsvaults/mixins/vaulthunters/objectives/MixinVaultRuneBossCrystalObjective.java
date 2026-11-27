package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.objective.VaultRuneBossCrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

@Mixin(value = VaultRuneBossCrystalObjective.class, remap = false)
public class MixinVaultRuneBossCrystalObjective {
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$beginDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.begin(ObjectiveDifficulty.multiplier(sigil, vault));
    }

    @Inject(method = "configure", at = @At("RETURN"))
    private void woldsvaults$endDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.end();
    }

    @ModifyArg(method = "lambda$configure$1", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/RuneBossObjective;of(FLiskallia/vault/core/world/roll/IntRoll;)Liskallia/vault/core/vault/objective/RuneBossObjective;"), index = 1)
    private IntRoll woldsvaults$scaleRunes(IntRoll runes) {
        return ObjectiveDifficulty.scale(runes, ObjectiveDifficulty.factor(ObjectiveDifficulty.current(), "rune_boss"));
    }
}

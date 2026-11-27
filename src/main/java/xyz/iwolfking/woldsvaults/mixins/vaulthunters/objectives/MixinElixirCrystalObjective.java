package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.objective.ElixirCrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

@Mixin(value = ElixirCrystalObjective.class, remap = false)
public class MixinElixirCrystalObjective {
    @ModifyVariable(method = "configure", at = @At("STORE"), ordinal = 0)
    private float woldsvaults$scaleTarget(float multiplier, Vault vault, RandomSource random, String sigil) {
        return (float) ObjectiveDifficulty.factor(ObjectiveDifficulty.multiplier(sigil, vault), "elixir");
    }
}

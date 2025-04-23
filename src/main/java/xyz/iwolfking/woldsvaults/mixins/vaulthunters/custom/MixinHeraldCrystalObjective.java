package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.VictoryObjective;
import iskallia.vault.item.crystal.objective.ElixirCrystalObjective;
import iskallia.vault.item.crystal.objective.HeraldCrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = HeraldCrystalObjective.class, remap = false)
public class MixinHeraldCrystalObjective {
    @Redirect(method = "lambda$configure$0", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/Objective;add(Liskallia/vault/core/vault/objective/Objective;)Liskallia/vault/core/vault/objective/Objective;", ordinal = 0))
    private Objective addCrateObjectiveToHerald(Objective instance, Objective child) {
        return instance.add(VictoryObjective.empty()).add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.BOSS, "herald", 100, true));
    }
}

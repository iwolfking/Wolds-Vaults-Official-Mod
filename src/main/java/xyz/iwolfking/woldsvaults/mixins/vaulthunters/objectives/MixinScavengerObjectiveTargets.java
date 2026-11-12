package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.ScavengerConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ScavengerObjective;
import iskallia.vault.core.vault.objective.scavenger.ScavengerGoal;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

import java.util.List;

@Mixin(value = ScavengerObjective.class, remap = false)
public class MixinScavengerObjectiveTargets {
    private static final String GENERATE_GOALS = "Liskallia/vault/config/ScavengerConfig;generateGoals(Lnet/minecraft/resources/ResourceLocation;ILiskallia/vault/core/random/RandomSource;)Ljava/util/List;";

    /** A runner's initial goals: padded to the difficulty's goal-count floor, then scaled by the scavenger exponent. */
    @WrapOperation(method = "generateGoal", at = @At(value = "INVOKE", target = GENERATE_GOALS))
    private List<ScavengerGoal> woldsvaults$scaleInitialGoals(ScavengerConfig config, ResourceLocation pool, int level, RandomSource random,
                                                             Operation<List<ScavengerGoal>> original, @Local(argsOnly = true) Vault vault) {
        return ObjectiveDifficulty.scavengerGoals(original.call(config, pool, level, random),
                () -> original.call(config, pool, level, random), ObjectiveDifficulty.multiplier(vault), level, "scavenger");
    }

    @WrapOperation(method = "reshuffleIncompleteGoals", at = @At(value = "INVOKE", target = GENERATE_GOALS))
    private List<ScavengerGoal> woldsvaults$scaleReshuffledGoals(ScavengerConfig config, ResourceLocation pool, int level, RandomSource random,
                                                                Operation<List<ScavengerGoal>> original, @Local(argsOnly = true) Vault vault) {
        List<ScavengerGoal> goals = original.call(config, pool, level, random);
        ObjectiveDifficulty.scaleScavengerGoals(goals, ObjectiveDifficulty.multiplier(vault), "scavenger");
        return goals;
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.vault.objective.ScavengerObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.objectives.UnhingedScavengerObjective;

@Mixin(value = ScavengerObjective.class, remap = false)
public class MixinScavengerObjective {
    // Wolf broke paradox vaults by replacing the scav objective with unhinged scav objective
    // we will also check if the objective has unhinged config, because doing it properly now would break currently running vaults on update
    @WrapOperation(method = "initServer", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/ScavengerObjective;getOr(Liskallia/vault/core/data/key/FieldKey;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object onInitServer(ScavengerObjective instance, FieldKey<ScavengerObjective.Config> fieldKey, Object o, Operation<Object> original) {
        UnhingedScavengerObjective.Config unhingedConfig = instance.get(UnhingedScavengerObjective.CONFIG);
        if (unhingedConfig == UnhingedScavengerObjective.Config.DIVINE_PARADOX) {
            return ScavengerObjective.Config.DIVINE_PARADOX;
        }
        return original.call(instance, fieldKey, o);
    }
}

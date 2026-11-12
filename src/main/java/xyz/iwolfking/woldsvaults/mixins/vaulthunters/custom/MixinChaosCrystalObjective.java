package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.ChaosConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.objective.ChaosCrystalObjective;
import iskallia.vault.task.ChaosTask;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ChaosTaskConfigAccessor;

import java.util.Optional;

@Mixin(value = ChaosCrystalObjective.class, remap = false)
public class MixinChaosCrystalObjective {
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$beginDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.begin(ObjectiveDifficulty.multiplier(sigil, vault));
    }

    @Inject(method = "configure", at = @At("RETURN"))
    private void woldsvaults$endDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.end();
    }

    /** The number of chaos tasks to clear rises by the chaos exponent; each task's own target is untouched. */
    @WrapOperation(method = "lambda$configure$1", at = @At(value = "INVOKE", target = "Liskallia/vault/config/ChaosConfig;generate(Lnet/minecraft/resources/ResourceLocation;I)Ljava/util/Optional;"))
    private Optional<ChaosTask> woldsvaults$scaleTaskCount(ChaosConfig instance, ResourceLocation pool, int level, Operation<Optional<ChaosTask>> original) {
        Optional<ChaosTask> task = original.call(instance, pool, level);
        task.ifPresent(chaos -> {
            ChaosTask.Config config = (ChaosTask.Config) chaos.getConfig();
            ChaosTaskConfigAccessor range = (ChaosTaskConfigAccessor) (Object) config;
            double factor = ObjectiveDifficulty.factor(ObjectiveDifficulty.current(), "chaos");
            config.setTaskRange(ObjectiveDifficulty.scale(range.woldsvaults$getMinTasks(), factor),
                    ObjectiveDifficulty.scale(range.woldsvaults$getMaxTasks(), factor));
        });
        return task;
    }
}

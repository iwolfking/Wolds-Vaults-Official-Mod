package xyz.iwolfking.woldsvaults.mixins.ispawner;

import iskallia.ispawner.block.entity.SpawnerBlockEntity;
import iskallia.ispawner.world.spawner.SpawnerExecution;
import iskallia.ispawner.world.spawner.SpawnerManager;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperSpawnSoftCap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ispawner")
        }
)
@Mixin(value = SpawnerManager.class, remap = false)
public class MixinSpawnerManager {

    /**
     * Collapses the spawner's per-modifier attempt map into one soft-capped term inside a hyper
     * vault. SpawnerManager#spawn accumulates {@code attempts += base * modifier} over
     * {@code attemptModifiers.values()}, so returning a single entry makes the burst
     * {@code base * (1 + effective)} while leaving every other spawner untouched.
     *
     * <p>Redirecting the iteration source rather than the stored local is deliberate: the
     * {@code attempts} local is also decremented once per spawned mob inside the loop below,
     * so a STORE-based injection would fire on every decrement. There is exactly one
     * {@code Map#values} call in the target method.
     */
    @Redirect(
            method = "spawn",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;")
    )
    private Collection<Double> woldsVaults$softCapSpawnAttempts(Map<UUID, Double> attemptModifiers,
                                                                Level world,
                                                                Random random,
                                                                SpawnerBlockEntity entity,
                                                                SpawnerExecution execution) {
        if (attemptModifiers.isEmpty() || !HyperSpawnSoftCap.appliesTo(world)) {
            return attemptModifiers.values();
        }
        double summed = 0.0;
        for (double modifier : attemptModifiers.values()) {
            summed += modifier;
        }
        double effective = HyperSpawnSoftCap.effectiveIncrease(summed);
        if (effective >= summed) {
            return attemptModifiers.values();
        }
        return List.of(effective);
    }
}

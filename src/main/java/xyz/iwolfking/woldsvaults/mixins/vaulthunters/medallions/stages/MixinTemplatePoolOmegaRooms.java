package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.template.context.TemplatePoolContext;
import iskallia.vault.core.world.template.data.IndirectTemplateEntry;
import iskallia.vault.core.world.template.data.TemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

@Mixin(value = TemplatePool.class, remap = false)
public class MixinTemplatePoolOmegaRooms {

    /**
     * Multiplies the weight {@code selectEntry} gives an {@code omega_rooms} reference by the omega room factor of the
     * context's vault. The weight is the one already scaled by the entry's own multipliers, and the pool is not changed.
     */
    @WrapOperation(
            method = "lambda$selectEntry$0",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/util/WeightedList;put(Ljava/lang/Object;Ljava/lang/Double;)Ljava/lang/Double;")
    )
    private static Double scaleOmegaRoomWeight(WeightedList<Object> weighted, Object child, Double weight,
                                               Operation<Double> original, @Local(argsOnly = true) TemplatePoolContext context) {
        if (context != null && child instanceof IndirectTemplateEntry entry
                && WoldConstants.OMEGA_ROOMS_POOL.equals(entry.getReferenceId())) {
            double factor = GreedMedallionEffects.omegaRoomWeightMultiplier(context.getVault());
            if (factor != 1.0D) {
                return original.call(weighted, child, weight * factor);
            }
        }
        return original.call(weighted, child, weight);
    }
}

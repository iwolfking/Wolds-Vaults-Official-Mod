package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.spellcraft.hit.OwnHits;

/** Counts nested hurt calls, so a hurt dealt from inside another entity's hurt can be told apart as a proc. */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityHurtDepth {

    @WrapMethod(method = "hurt")
    private boolean woldsvaults$countHurtDepth(DamageSource source, float amount, Operation<Boolean> original) {
        OwnHits.enterHurt();
        try {
            return original.call(source, amount);
        } finally {
            OwnHits.exitHurt();
        }
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.velara;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.gods.trees.velara.Sanitation;

/**
 * Sanitation's duration cut. The base mod's effect-check event can only deny an effect, never
 * shorten one, so the instance itself is rewritten on the way into {@code addEffect} -  the same
 * method the base mod already mixins.
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntitySanitation {

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance velaraSanitation(MobEffectInstance instance) {
        return Sanitation.shorten((LivingEntity) (Object) this, instance);
    }
}

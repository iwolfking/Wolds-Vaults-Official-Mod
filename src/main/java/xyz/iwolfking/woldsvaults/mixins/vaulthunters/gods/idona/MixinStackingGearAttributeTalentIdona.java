package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import iskallia.vault.skill.talent.type.StackingGearAttributeTalent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaStackTuning;

@Mixin(value = StackingGearAttributeTalent.class, remap = false)
public class MixinStackingGearAttributeTalentIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Stack Hoarder makes stacks last 50% longer. Applied after the base effect-duration
     * adjustment so it multiplies the finished duration rather than competing with the
     * effect_duration attribute, which would bleed into every mob effect in the game.
     */
    @Inject(method = "getDurationTicks", at = @At("RETURN"), cancellable = true)
    private void applyStackHoarder(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(IdonaStackTuning.stackDuration(entity, cir.getReturnValue()));
    }
}

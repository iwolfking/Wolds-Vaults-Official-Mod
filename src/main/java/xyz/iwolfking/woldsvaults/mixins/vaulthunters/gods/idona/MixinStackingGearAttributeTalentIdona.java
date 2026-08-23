package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.talent.type.StackingGearAttributeTalent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaStackTuning;

@Mixin(value = StackingGearAttributeTalent.class, remap = false)
public class MixinStackingGearAttributeTalentIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Stack Hoarder makes stacks last 50% longer. Applied after the base effect-duration
     * adjustment so it multiplies the finished duration rather than competing with the
     * effect_duration attribute, which would bleed into every mob effect in the game. A
     * return-value modifier so any other modifier of this getter chains with it rather than one
     * of them cancelling past the other.
     */
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int applyStackHoarder(int duration, LivingEntity entity) {
        return IdonaStackTuning.stackDuration(entity, duration);
    }
}

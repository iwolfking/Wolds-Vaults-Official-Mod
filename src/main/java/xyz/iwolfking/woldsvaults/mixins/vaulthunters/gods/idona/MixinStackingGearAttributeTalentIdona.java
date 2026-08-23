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
     * @reason Stack Hoarder makes stacks last 50% longer, multiplying the finished duration.
     */
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int applyStackHoarder(int duration, LivingEntity entity) {
        return IdonaStackTuning.stackDuration(entity, duration);
    }
}

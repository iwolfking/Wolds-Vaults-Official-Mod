package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestAttackableTargetGoalAccessor<T extends LivingEntity> {
    @Accessor("targetType")
    Class<T> getTargetType();

    @Accessor("targetConditions")
    TargetingConditions getTargetingConditions();
}

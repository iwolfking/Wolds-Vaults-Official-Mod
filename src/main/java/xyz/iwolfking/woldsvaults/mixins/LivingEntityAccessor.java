package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastHurt")
    void setLastHurt(float lastHurt);
}

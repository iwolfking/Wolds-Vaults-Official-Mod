package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = LightningOrbEntity.class, remap = false)
public interface LightningOrbEntityAccessor {
    @Accessor("direction")
    void woldsvaults$setDirection(Vec3 direction);

    @Accessor("hitEntities")
    Set<Integer> woldsvaults$getHitEntities();

    @Accessor("SPEED")
    static EntityDataAccessor<Float> woldsvaults$getSpeed() {
        throw new AssertionError();
    }

    @Accessor("DURATION")
    static EntityDataAccessor<Integer> woldsvaults$getDuration() {
        throw new AssertionError();
    }

    @Accessor("AGE")
    static EntityDataAccessor<Integer> woldsvaults$getAge() {
        throw new AssertionError();
    }
}

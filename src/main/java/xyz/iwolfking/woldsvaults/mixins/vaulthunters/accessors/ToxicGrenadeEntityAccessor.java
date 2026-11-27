package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.entity.entity.ToxicGrenadeEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ToxicGrenadeEntity.class, remap = false)
public interface ToxicGrenadeEntityAccessor {
    @Accessor("DURATION")
    static EntityDataAccessor<Integer> woldsvaults$duration() {
        throw new AssertionError();
    }

    @Invoker("explode")
    void woldsvaults$explode();
}

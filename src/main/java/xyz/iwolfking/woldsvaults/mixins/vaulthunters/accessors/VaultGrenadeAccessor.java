package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.entity.entity.VaultGrenade;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VaultGrenade.class, remap = false)
public interface VaultGrenadeAccessor {
    @Accessor("DURATION")
    static EntityDataAccessor<Integer> woldsvaults$duration() {
        throw new AssertionError();
    }

    @Invoker("explode")
    void woldsvaults$explode();
}

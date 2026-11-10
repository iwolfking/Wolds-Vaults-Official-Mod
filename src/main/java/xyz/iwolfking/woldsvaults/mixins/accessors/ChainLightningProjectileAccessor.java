package xyz.iwolfking.woldsvaults.mixins.accessors;

import iskallia.vault.skill.ability.effect.ChainLightningAbility;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChainLightningAbility.ChainLightningProjectile.class, remap = false)
public interface ChainLightningProjectileAccessor {
    @Accessor("START_X")
    static EntityDataAccessor<Float> woldsvaults$startX() {
        throw new AssertionError();
    }

    @Accessor("START_Y")
    static EntityDataAccessor<Float> woldsvaults$startY() {
        throw new AssertionError();
    }

    @Accessor("START_Z")
    static EntityDataAccessor<Float> woldsvaults$startZ() {
        throw new AssertionError();
    }
}

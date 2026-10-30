package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EvokerFangs.class)
public interface EvokerFangsAccessor {
    @Accessor("clientSideAttackStarted")
    boolean getClientSideAttackStarted();

    @Accessor("sentSpikeEvent")
    boolean getSentSpikeEvent();

    @Accessor("sentSpikeEvent")
    void setSentSpikeEvent(boolean value);


    @Accessor("lifeTicks")
    int getLifeTicks();

    @Accessor("lifeTicks")
    void setLifeTicks(int ticks);

    @Accessor("warmupDelayTicks")
    int getWarmupDelayTicks();

    @Accessor("warmupDelayTicks")
    void setWarmupDelayTicks(int ticks);
}

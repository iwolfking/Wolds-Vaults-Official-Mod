package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    // how long the server has seen the player float; kept at 0 while a Bullet Bill flies
    @Accessor("aboveGroundTickCount")
    void woldsvaults$setAboveGroundTickCount(int ticks);
}

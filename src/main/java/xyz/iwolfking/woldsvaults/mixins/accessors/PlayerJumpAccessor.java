package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerJumpAccessor {
    @Accessor("jumpTriggerTime")
    void woldsvaults$setJumpTriggerTime(int ticks);
}

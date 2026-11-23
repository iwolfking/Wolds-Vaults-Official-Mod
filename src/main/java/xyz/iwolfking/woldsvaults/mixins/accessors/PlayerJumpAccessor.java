package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerJumpAccessor {
    // the window vanilla's double tap for creative flight counts down; kept at 0 while a Bullet Bill flies
    @Accessor("jumpTriggerTime")
    void woldsvaults$setJumpTriggerTime(int ticks);
}

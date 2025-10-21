package xyz.iwolfking.woldsvaults.mixins.bettercombat;

import net.bettercombat.client.collision.TargetFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.helper.CollisionHelper;

@Mixin(value = TargetFinder.RadialFilter.class, remap = true)
public class MixinRadialFilter {
    /**
     * @author iwolfking
     * @reason Modify the clip method used
     */
    @Redirect(method = "rayContainsNoObstacle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private static BlockHitResult rayContainsNoObstacle(ClientLevel instance, ClipContext clipContext) {
        return CollisionHelper.specialClip(instance, clipContext);
    }


}

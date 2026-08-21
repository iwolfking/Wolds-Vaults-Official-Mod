package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmStats;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityAirSpeed {

    /**
     * @author PoorMansPhysicist
     * @reason the mythic charm's air movement speed multiplier. Airborne player movement uses the
     * flat flying-speed constant instead of the movement speed attribute, and this friction branch
     * is the one read of it, so multiplying here on both sides scales air control without touching
     * ground speed. The snapshot is synced, so client prediction and server agree.
     */
    @ModifyReturnValue(method = "getFrictionInfluencedSpeed", at = @At("RETURN"))
    private float woldsVaults$multiplyAirSpeed(float speed) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.isOnGround() || !(self instanceof Player)) {
            return speed;
        }
        float multiplier = MythicCharmStats.snapshotSum(self, ModGearAttributes.AIR_MOVEMENT_SPEED_MULTIPLIER);
        return multiplier > 0.0F ? speed * (1.0F + multiplier) : speed;
    }
}

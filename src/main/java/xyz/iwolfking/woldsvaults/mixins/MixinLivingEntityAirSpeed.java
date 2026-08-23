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
     * @reason the mythic charm's air movement speed multiplier; airborne movement reads the flat
     * flying-speed constant here rather than the movement speed attribute.
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

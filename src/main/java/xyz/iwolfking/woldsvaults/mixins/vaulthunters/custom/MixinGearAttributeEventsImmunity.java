package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.GearAttributeEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionAura;

/**
 * The second half of the effect-piercing aura.
 *
 * <p>Forcing a harmful effect onto a player is not enough on its own: the base mod sweeps every effect
 * a player has immunity to off them again, every single tick. Without this the aura would apply an
 * effect and watch it vanish before the next frame.
 *
 * <p>The sweep is suspended only while the player is standing inside an aura, and only then. Step out
 * and it resumes on the next tick, which is why immunity-granted protection reasserts itself
 * immediately on leaving while avoidance-granted protection leaves the effect running - avoidance only
 * ever rolls at the moment of application, and that moment has passed.</p>
 */
@Mixin(value = GearAttributeEvents.class, remap = false)
public abstract class MixinGearAttributeEventsImmunity {

    @Inject(method = "removeImmuneEffects", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$auraSuspendsImmunitySweep(LivingEvent.LivingUpdateEvent event, CallbackInfo ci) {
        if (VaultChampionAura.isEmpty()) {
            return;
        }
        if (event.getEntityLiving() instanceof ServerPlayer player && VaultChampionAura.contains(player)) {
            ci.cancel();
        }
    }
}

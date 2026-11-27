package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.GearAttributeEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionAura;

/** Suspends the per-tick sweep that strips immune effects, but only while the player stands in an aura. */
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

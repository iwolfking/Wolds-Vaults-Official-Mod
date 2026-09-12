package xyz.iwolfking.woldsvaults.mixins.easypiglins;

import de.maxhenkel.easypiglins.events.PiglinEvents;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "easy_piglins")
        }
)
@Mixin(value = PiglinEvents.class)
public class MixinPiglinEvents {
    @Inject(method = "onClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;angerNearbyPiglins(Lnet/minecraft/world/entity/player/Player;Z)V"), cancellable = true)
    private void dontAllowPiglinPickupInVaults(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if(ServerVaults.get(event.getTarget().getLevel()).isPresent()) {
            ci.cancel();
        }
    }
}

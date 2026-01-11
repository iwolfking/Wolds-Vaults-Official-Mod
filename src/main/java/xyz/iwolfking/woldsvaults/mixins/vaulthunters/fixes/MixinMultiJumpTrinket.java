package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.gear.trinket.effects.MultiJumpTrinket;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.events.KeyInputEvents;

@Mixin(value = MultiJumpTrinket.class, remap = false)
public class MixinMultiJumpTrinket {
    @Shadow private static boolean clientIsJumpHeld = false;

    /**
     * Fixes an issue present when holding the jump button and landing on the ground again,
     * <p>
     * If the player held the jump button as they land, they would not receive the additional velocity boost upwards,
     * this mixin aims to resolve this.
     */
    @Inject(
            method = "onClientTick",
            at = @At(value = "FIELD",
                    target = "Liskallia/vault/gear/trinket/effects/MultiJumpTrinket;clientJumpCount:I",
                    ordinal = 0),
            require = 1
    )
    private void afterResetJumpCount(Player player, CallbackInfo ci) {
        if(KeyInputEvents.isFeatherFixEnabled) {
            clientIsJumpHeld = false;
        }
    }

}

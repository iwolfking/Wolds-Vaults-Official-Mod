package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;

@Mixin(ServerItemCooldowns.class)
public abstract class MixinServerItemCooldownsRiptide {
    @Shadow @Final private ServerPlayer player;

    /** Records whether a new vault-trident cooldown is a riptide cooldown, before the base mod refreshes the snapshot. */
    @Inject(method = "onCooldownStarted", at = @At("HEAD"))
    private void markRiptideCooldown(Item item, int ticks, CallbackInfo ci) {
        RiptideDash.onCooldownStarted(this.player, item);
    }

    @Inject(method = "onCooldownEnded", at = @At("HEAD"))
    private void clearRiptideCooldown(Item item, CallbackInfo ci) {
        RiptideDash.onCooldownEnded(this.player, item);
    }
}

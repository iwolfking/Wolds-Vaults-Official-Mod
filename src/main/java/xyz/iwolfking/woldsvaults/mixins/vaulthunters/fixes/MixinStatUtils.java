package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.util.StatUtils;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.stats.StatDisplayValues;

@Mixin(value = StatUtils.class, remap = false)
public class MixinStatUtils {
    /**
     * The damage estimate counts the capped lucky hit chance (effects and talents included) and applies Purist
     * multiplicatively, as Wold's Vaults combat does.
     */
    @Inject(method = "getAverageDps", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$combatAccurateDps(Player player, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(StatDisplayValues.averageDps(player));
    }
}

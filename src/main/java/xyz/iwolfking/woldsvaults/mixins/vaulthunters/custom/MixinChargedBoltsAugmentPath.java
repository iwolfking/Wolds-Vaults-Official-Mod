package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.ChargedBoltsAbility;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.effect.ChargedBoltPaths;

@Mixin(value = ChargedBoltsAbility.class, remap = false)
public abstract class MixinChargedBoltsAugmentPath {

    /** Inside a spell cast, Charged Bolts fly the spellcraft path, which carries the step's glyphs; otherwise VH's own. */
    @Inject(method = "castBolts", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$augmentedBolts(ServerPlayer player, CallbackInfo ci) {
        CastScope.current().filter(active -> active.player() == player).ifPresent(active -> {
            ChargedBoltPaths.cast((ChargedBoltsAbility) (Object) this, player, active.modifiers());
            ci.cancel();
        });
    }
}

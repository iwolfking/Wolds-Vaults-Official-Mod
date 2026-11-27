package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.projectile;

import iskallia.vault.skill.ability.effect.ChargedBoltsAbility;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ChargedBoltPaths;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;

@Mixin(value = ChargedBoltsAbility.class, remap = false)
public abstract class MixinChargedBoltsGlyphPath {

    /**
     * Inside a spell cast, Charged Bolts fly the spellcraft path, which carries the step's glyphs; otherwise VH's own.
     */
    @Inject(method = "castBolts", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$glyphedBolts(ServerPlayer player, CallbackInfo ci) {
        CastScope.Active active = CastScope.forPlayer(player);
        if (active != null) {
            ChargedBoltPaths.cast((ChargedBoltsAbility) (Object) this, player, active.modifiers(), active.spec(), active.shot());
            ci.cancel();
        }
    }
}

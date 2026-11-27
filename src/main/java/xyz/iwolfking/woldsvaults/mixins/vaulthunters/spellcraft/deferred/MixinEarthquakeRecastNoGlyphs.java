package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.deferred;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.EarthquakeAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;

/**
 * The recast etching's extra shock is a proc: it runs scheduled like the quake's own later shocks and deals the same
 * hit, so it is told apart by its reduced effect and keeps no spell glyphs.
 */
@Mixin(value = EarthquakeAbility.class, remap = false)
public class MixinEarthquakeRecastNoGlyphs {

    @WrapMethod(method = "triggerShock")
    private void woldsvaults$recastWithoutGlyphs(ServerPlayer player, Vec3 pos, int shockIndex, boolean fullEffect, Operation<Void> original) {
        if (fullEffect) {
            original.call(player, pos, shockIndex, fullEffect);
            return;
        }
        DeferredReach.suppress(() -> original.call(player, pos, shockIndex, fullEffect));
    }
}

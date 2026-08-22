package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.talent.type.luckyhit.SweepingLuckyHitTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaState;

@Mixin(value = SweepingLuckyHitTalent.class, remap = false)
public class MixinSweepingLuckyHitTalentIdona {

    /**
     * Brackets the sweep so the hit handler can tell cleave from other area-flagged damage.
     * Cleave Expert and True Rage both act on area damage but must not apply to each other's hits,
     * and the sweep runs inside a single area flag with nothing else to distinguish it.
     *
     * <p>The bracket wraps the call rather than using HEAD and RETURN injections. RETURN does not
     * run when the target throws, so a leaked flag would make True Rage return early on every
     * later hit and Cleave Expert pay on every later hit, for the life of the server thread.
     */
    @WrapMethod(method = "onLuckyHit")
    private void idonaBracketCleave(LivingHurtEvent event, Operation<Void> original) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            original.call(event);
            return;
        }
        ServerPlayer previous = IdonaState.pushCleave(attacker);
        try {
            original.call(event);
        } finally {
            IdonaState.popCleave(previous);
        }
    }
}

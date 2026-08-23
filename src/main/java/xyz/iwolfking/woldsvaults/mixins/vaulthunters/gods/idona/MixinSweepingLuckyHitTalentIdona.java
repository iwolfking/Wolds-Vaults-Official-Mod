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

    /** Brackets the sweep with a flag so the hit handler can tell cleave from other area damage. */
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

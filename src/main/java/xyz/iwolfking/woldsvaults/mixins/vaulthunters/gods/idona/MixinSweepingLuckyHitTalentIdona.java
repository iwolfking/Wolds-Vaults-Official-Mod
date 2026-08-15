package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import iskallia.vault.skill.talent.type.luckyhit.SweepingLuckyHitTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaState;

@Mixin(value = SweepingLuckyHitTalent.class, remap = false)
public class MixinSweepingLuckyHitTalentIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Cleave Expert and True Rage both act on area-flagged damage but must not apply to
     * each other's hits. The sweep runs inside a single area flag with nothing to distinguish it
     * from an attack-damage ability, so it is bracketed here and the hit handler reads the bracket.
     */
    @Inject(method = "onLuckyHit", at = @At("HEAD"))
    private void beginCleave(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            IdonaState.beginCleave(attacker);
        }
    }

    @Inject(method = "onLuckyHit", at = @At("RETURN"))
    private void endCleave(LivingHurtEvent event, CallbackInfo ci) {
        IdonaState.endCleave();
    }
}

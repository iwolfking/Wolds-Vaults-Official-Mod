package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaLuckyHit;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalentIdona {
    /**
     * @author PoorMansPhysicist
     * @reason the tail is reached exactly once per lucky hit that procced
     */
    @Inject(method = "doLuckyHit", at = @At("TAIL"))
    private static void onLuckyHitResolved(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            Milestones.onLuckyHit(attacker, event);
            IdonaLuckyHit.onLuckyHitResolved(attacker, event);
        }
    }
}

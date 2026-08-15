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
     * @reason the only place a lucky hit is known to have procced. Every earlier return in this
     * method is a failed roll or a disqualified hit, so the tail is reached exactly once per real
     * lucky hit -  which is what the Five Leaf Clover milestone needs (it otherwise only sees the
     * addon's own three talents) and where Overcrit and Luckiest Hit apply.
     */
    @Inject(method = "doLuckyHit", at = @At("TAIL"))
    private static void onLuckyHitResolved(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            Milestones.onLuckyHit(attacker, event);
            IdonaLuckyHit.onLuckyHitResolved(attacker, event);
        }
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.gear.simon.SimonsSet;

/** Lets Simon's Set crystals bud and gain facets from Lucky Hits; the tail is reached once per lucky hit that procced. */
@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalentSimon {
    @Inject(method = "doLuckyHit", at = @At("TAIL"))
    private static void woldsvaults$simonLuckyHit(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            SimonsSet.onLuckyHit(attacker, event);
        }
    }
}

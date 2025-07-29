package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import iskallia.vault.effect.ImmortalityEffect;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.entity.champion.IChampionAffix;
import iskallia.vault.entity.champion.PotionAuraAffix;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ImmortalityEffect.class, remap = false)
public class MixinImmortalityEffect {
    @Inject(method = "onAdded", at = @At("TAIL"))
    private static void preventEtherealChampsGainingEthereal(PotionEvent.PotionApplicableEvent event, CallbackInfo ci) {
        LivingEntity entity = event.getEntityLiving();
        if(entity instanceof ChampionLogic.IChampionLogicHolder championLogicHolder) {
            for(IChampionAffix affix : championLogicHolder.getChampionLogic().getAffixes()) {
                if(affix instanceof PotionAuraAffix potionAuraAffix) {
                    if(potionAuraAffix.getMobEffect() != null && potionAuraAffix.getMobEffect().equals(ModEffects.IMMORTALITY)) {
                        event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }

}

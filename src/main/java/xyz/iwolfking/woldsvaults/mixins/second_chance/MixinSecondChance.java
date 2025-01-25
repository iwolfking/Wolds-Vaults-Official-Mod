package xyz.iwolfking.woldsvaults.mixins.second_chance;

import iskallia.vault.etching.EtchingHelper;
import iskallia.vault.init.ModEtchings;
import iskallia.vault.world.data.PlayerEtchingData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.infernalstudios.secondchanceforge.SecondChanceEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SecondChanceEvents.class, remap = false)
public class MixinSecondChance {
    @Inject(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingDamageEvent;setAmount(F)V"))
    private void onTriggerSecondChance(LivingDamageEvent event, CallbackInfo ci) {
        if(event.getEntityLiving() instanceof Player player) {
            if(PlayerEtchingData.get((ServerLevel) player.level).getEtchingSets(player).contains(ModEtchings.PHOENIX)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 9));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1));
            }
        }
    }
}

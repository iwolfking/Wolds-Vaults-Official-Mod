package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.SwampZombieEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SwampZombieEntity.class, remap = false)
public abstract class MixinSwampZombie extends Zombie {
    public MixinSwampZombie(EntityType<? extends Zombie> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @WrapOperation(method = "lambda$tick$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", remap = true))
    private boolean checkForLineOfSight(Player instance, MobEffectInstance mobEffectInstance, Operation<Boolean> original) {
        if (this.hasLineOfSight(instance)) {
            return original.call(instance, mobEffectInstance);
        } else {
            return false;
        }
    }
}

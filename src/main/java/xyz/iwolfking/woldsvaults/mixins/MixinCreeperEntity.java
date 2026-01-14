package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class MixinCreeperEntity extends Monster implements PowerableMob {
    protected MixinCreeperEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "spawnLingeringCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    private void preventLingeringForBlacklistedEffects(AreaEffectCloud instance, MobEffectInstance pEffectInstance) {
        if(pEffectInstance.getEffect().getRegistryName() != null && pEffectInstance.getEffect().getRegistryName().getNamespace().equals("minecraft")) {
            instance.addEffect(pEffectInstance);
        }
    }
}

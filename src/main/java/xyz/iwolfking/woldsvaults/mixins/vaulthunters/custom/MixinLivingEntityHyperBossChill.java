package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;

import java.util.ArrayList;

@Mixin(LivingEntity.class)
public class MixinLivingEntityHyperBossChill {
    /**
     * Chilled on a hyperboss lands at level clamp(floor(L / 5), 1, 5), where L is the source level (amplifier + 1),
     * so a level 21 Frost Nova chills at level 4. Every other field of the instance is kept.
     */
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance woldsvaults$scaleHyperBossChill(MobEffectInstance instance) {
        if (instance == null || instance.getEffect() != ModEffects.CHILLED || !((Object) this instanceof VaultBossEntity boss)) {
            return instance;
        }
        if (!HyperVaultEvents.isInHyperVault(boss)) {
            return instance;
        }
        int amplifier = Mth.clamp((instance.getAmplifier() + 1) / 5, 1, 5) - 1;
        if (amplifier == instance.getAmplifier()) {
            return instance;
        }
        MobEffectInstance scaled = new MobEffectInstance(instance.getEffect(), instance.getDuration(), amplifier, instance.isAmbient(), instance.isVisible(), instance.showIcon());
        scaled.setCurativeItems(new ArrayList<>(instance.getCurativeItems()));
        return scaled;
    }
}

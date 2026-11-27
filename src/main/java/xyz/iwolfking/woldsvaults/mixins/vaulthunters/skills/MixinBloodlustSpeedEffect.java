package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.ability.effect.RampageBloodlustAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.abilities.BloodlustSpeed;

@Mixin(MobEffect.class)
public abstract class MixinBloodlustSpeedEffect {

    /** Bloodlust's speed effect grants the player's Bloodlust {@code speedPerStack} per stack instead of a fixed 1%. */
    @WrapOperation(method = "addAttributeModifiers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/effect/MobEffect;getAttributeModifierValue(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D"))
    private double woldsvaults$bloodlustSpeedPerStack(MobEffect effect, int amplifier, AttributeModifier modifier, Operation<Double> original,
                                                      @Local(argsOnly = true) LivingEntity entity) {
        if (!(effect instanceof RampageBloodlustAbility.BloodlustSpeedEffect) || !(entity instanceof ServerPlayer player)) {
            return original.call(effect, amplifier, modifier);
        }
        return BloodlustSpeed.speedPerStack(player, modifier.getAmount()) * (amplifier + 1);
    }
}

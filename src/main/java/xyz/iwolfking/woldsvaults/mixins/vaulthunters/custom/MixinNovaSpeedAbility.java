package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.init.ModEffects;
import iskallia.vault.skill.ability.effect.NovaSpeedAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.WeakHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = NovaSpeedAbility.class, remap = false)
public class MixinNovaSpeedAbility {
    @Unique
    private static final Map<LivingEntity, MobEffectInstance> woldsvaults$NOVA_VULNERABLE = new WeakHashMap<>();

    @Unique
    private static final Map<LivingEntity, Integer> woldsvaults$NOVA_VULNERABLE_AMPLIFIER = new WeakHashMap<>();

    /**
     * Frozen Orb's Vulnerable. Adds the summed level once to the target's existing Vulnerable amplifier (10 with a
     * 6 roll becomes 16). The instance a nova applied is remembered per target, and later novas only refresh its
     * duration while it is still the target's Vulnerable at that amplifier. With no Vulnerable on the target the
     * base application (summed level - 1) is used and remembered the same way.
     */
    @WrapOperation(method = "lambda$doAction$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", ordinal = 1, remap = true))
    private boolean woldsvaults$additiveFrozenOrbVulnerable(LivingEntity target, MobEffectInstance incoming, Operation<Boolean> original) {
        if (incoming.getEffect() != ModEffects.VULNERABLE) {
            WoldLogger.error("Frozen Orb Vulnerable wrap saw {} instead of Vulnerable; applying it unchanged", incoming.getEffect().getRegistryName());
            return original.call(target, incoming);
        }
        MobEffectInstance existing = target.getEffect(ModEffects.VULNERABLE);
        int amplifier;
        if (existing == null) {
            amplifier = incoming.getAmplifier();
        } else if (woldsvaults$NOVA_VULNERABLE.get(target) == existing
                && Integer.valueOf(existing.getAmplifier()).equals(woldsvaults$NOVA_VULNERABLE_AMPLIFIER.get(target))) {
            amplifier = existing.getAmplifier();
        } else {
            amplifier = existing.getAmplifier() + incoming.getAmplifier() + 1;
        }
        MobEffectInstance applied = new MobEffectInstance(ModEffects.VULNERABLE, incoming.getDuration(), amplifier, incoming.isAmbient(), incoming.isVisible(), incoming.showIcon());
        boolean result = original.call(target, applied);
        MobEffectInstance current = target.getEffect(ModEffects.VULNERABLE);
        if (current != null) {
            woldsvaults$NOVA_VULNERABLE.put(target, current);
            woldsvaults$NOVA_VULNERABLE_AMPLIFIER.put(target, current.getAmplifier());
        }
        return result;
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.mana.Mana;
import iskallia.vault.skill.ability.effect.NovaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;

import java.util.List;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = NovaAbility.class, remap = false)
public abstract class MixinNovaAbility {

    @Shadow
    protected abstract float getDamageModifier(float radius, float dist);

    @Redirect(method = "lambda$doAction$4", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/ability/effect/NovaAbility;getDamageModifier(FF)F"))
    private float callAdjustedDamageModifier(NovaAbility instance, float radius, float dist, @Local(argsOnly = true) ServerPlayer player) {
        return AbilityHelper.getScaledByLevelDamageFalloff(this.getDamageModifier(radius, dist), player, "Nova_Base");
    }

    /**
     * Nova's low-mana damage etching compounded one {@code d += d x mult} step per copy worn. Keeps only the
     * strongest copy whose mana threshold is met, so several copies give the largest bonus once.
     */
    @WrapOperation(method = "lambda$doAction$4", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/etching/EtchingHelper;getEtchings(Lnet/minecraft/world/entity/LivingEntity;Liskallia/vault/gear/attribute/VaultGearAttribute;)Ljava/util/List;"))
    private <T> List<VaultGearAttributeInstance<T>> woldsvaults$strongestLowManaCopy(LivingEntity entity, VaultGearAttribute<T> attribute, Operation<List<VaultGearAttributeInstance<T>>> original, @Local(argsOnly = true) ServerPlayer player) {
        List<VaultGearAttributeInstance<T>> copies = original.call(entity, attribute);
        if (attribute != (Object) EtchingGearAttributes.NOVA_LOW_MANA_DAMAGE || copies.size() < 2) {
            return copies;
        }
        float max = Mana.getMax(player);
        float current = Mana.get(player);
        VaultGearAttributeInstance<T> strongest = null;
        float strongestMult = Float.NEGATIVE_INFINITY;
        for (VaultGearAttributeInstance<T> copy : copies) {
            if (!(copy.getValue() instanceof Pair<?, ?> pair) || !(pair.getFirst() instanceof Float mult) || !(pair.getSecond() instanceof Float threshold)) {
                WoldLogger.error("Nova low-mana etching copy has an unexpected value {}; keeping every copy", copy.getValue());
                return copies;
            }
            if (max > 0.0F && current / max < threshold && mult > strongestMult) {
                strongest = copy;
                strongestMult = mult;
            }
        }
        return strongest == null ? List.of() : List.of(strongest);
    }

    /** Nova falloff is measured from the cast's source position (a Bundled Spell landing), not from the player. */
    @WrapOperation(method = "lambda$doAction$4", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceTo(Lnet/minecraft/world/entity/Entity;)F", remap = true))
    private float woldsvaults$falloffFromSource(ServerPlayer player, Entity entity, Operation<Float> original,
                                                @Local(argsOnly = true) SkillContext context) {
        return context.getSource().getPos().map(pos -> (float) entity.position().distanceTo(pos)).orElseGet(() -> original.call(player, entity));
    }
}

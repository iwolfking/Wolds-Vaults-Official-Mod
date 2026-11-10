package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.ability.effect.ImplodeAbility;
import iskallia.vault.skill.ability.effect.NovaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;

@Mixin(value = ImplodeAbility.class, remap = false)
public abstract class MixinImplodeAbility {

    @Shadow
    protected abstract float getDamageModifier(float radius, float dist);

    @Redirect(method = "lambda$doAction$1", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/ability/effect/ImplodeAbility;getDamageModifier(FF)F"))
    private float callAdjustedDamageModifier(ImplodeAbility instance, float radius, float dist, @Local ServerPlayer player) {
        return AbilityHelper.getScaledByLevelDamageFalloff(this.getDamageModifier(radius, dist), player, "Mana_Shield_Implode");
    }

    /** Implode falloff is measured from the cast's source position (a Bundled Spell landing), not from the player. */
    @WrapOperation(method = "lambda$doAction$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceTo(Lnet/minecraft/world/entity/Entity;)F", remap = true))
    private float woldsvaults$falloffFromSource(ServerPlayer player, Entity entity, Operation<Float> original,
                                                @Local(argsOnly = true) SkillContext context) {
        return context.getSource().getPos().map(pos -> (float) entity.position().distanceTo(pos)).orElseGet(() -> original.call(player, entity));
    }
}

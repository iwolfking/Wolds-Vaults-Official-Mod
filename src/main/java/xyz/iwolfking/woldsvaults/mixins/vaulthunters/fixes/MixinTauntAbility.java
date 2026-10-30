package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.init.ModEffects;
import java.util.function.Predicate;

import iskallia.vault.skill.ability.effect.TauntAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/// aida - allow taunt to target vault bosses, but not teleport them
@Mixin(value = TauntAbility.class, remap = false)
public class MixinTauntAbility {

    /// aida - make harold targetable with taunt
    @Redirect(method = "lambda$doAction$1",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;selector(Ljava/util/function/Predicate;)Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;", remap = true))
    private TargetingConditions doTargetHarold(TargetingConditions conditions, Predicate<LivingEntity> x) {
        return conditions.selector(NEW_MONSTER_PREDICATE);
    }
    @Unique
    private static final Predicate<LivingEntity> NEW_MONSTER_PREDICATE = entity -> entity.getType().getCategory() == MobCategory.MONSTER
            && entity instanceof Mob
            && !entity.hasEffect(ModEffects.TAUNT_CHARM)
            && !entity.hasEffect(ModEffects.TAUNT_REPEL_MOB)
            && !entity.getTags().contains("ethereal_immune");

    /// aida - make harold and the vessel un-teleportable by taunt
    @WrapOperation(method = "lambda$doAction$1",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/Mob;setPos(DDD)V", remap = true))
    private void dontYoinkBosses(Mob mob, double x, double y, double z, Operation<Void> original) {
        if(!(mob instanceof TheVesselEntity || mob instanceof VaultBoss))
            original.call(mob, x, y, z);
    }
}

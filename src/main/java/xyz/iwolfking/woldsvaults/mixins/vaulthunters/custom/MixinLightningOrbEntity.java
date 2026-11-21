package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.LightningOrbEntity;
import iskallia.vault.gear.etching.EtchingHitShare;
import iskallia.vault.skill.ability.effect.LightningOrbsAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = LightningOrbEntity.class, remap = false)
public class MixinLightningOrbEntity {

    @Unique
    private static List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/ability/effect/LightningOrbsAbility;)V", at = @At("TAIL"))
    private void woldsVaults$captureElements(net.minecraft.world.level.Level level, ServerPlayer shooter, LightningOrbsAbility ability, CallbackInfo ci) {
        if (ability instanceof IWoldAbilityEnhancements enhanced) {
            woldsVaults$elementalTypes = new ArrayList<>(enhanced.getElementTypes());
        }
    }

    @WrapOperation(method = "lambda$applyLightningDamage$0", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/etching/EtchingHitShare;hurt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = true))
    private static boolean woldsVaults$wrapElementalFlags(EtchingHitShare instance, Entity target, DamageSource source, float unetchedDamage, Operation<Boolean> original) {
        boolean[] result = new boolean[]{false};
        ElementHelper.runWithElements(woldsVaults$elementalTypes, () -> result[0] = original.call(instance, source, unetchedDamage));
        return result[0];
    }
}

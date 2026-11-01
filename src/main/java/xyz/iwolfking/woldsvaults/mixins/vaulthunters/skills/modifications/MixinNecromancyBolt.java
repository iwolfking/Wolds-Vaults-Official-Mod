package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import iskallia.vault.skill.ability.effect.NecromancyAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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

@Mixin(targets = "iskallia.vault.skill.ability.effect.NecromancyAbility$NecromancyBolt", remap = false)
public class MixinNecromancyBolt {

    @Unique
    private List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @Unique
    private ServerPlayer woldsVaults$shooter = null;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/ability/effect/NecromancyAbility;)V", at = @At("TAIL"))
    private void woldsVaults$captureElements(Level level, ServerPlayer shooter, NecromancyAbility ability, CallbackInfo ci) {
        this.woldsVaults$shooter = shooter;
        if (ability instanceof IWoldAbilityEnhancements enhanced) {
            this.woldsVaults$elementalTypes = new ArrayList<>(enhanced.getElementTypes());
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"))
    private void woldsVaults$applyElement(HitResult result, CallbackInfo ci) {
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target && woldsVaults$shooter != null && NecromancyAbility.isValidTarget(woldsVaults$shooter, target)) {
            ElementHelper.runWithElements(woldsVaults$elementalTypes, () -> ElementHelper.applyElementalEffects(target, woldsVaults$shooter, 1.0));
        }
    }
}
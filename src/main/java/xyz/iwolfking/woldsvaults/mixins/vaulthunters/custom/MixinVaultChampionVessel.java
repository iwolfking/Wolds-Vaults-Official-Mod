package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.VaultTrueDamage;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/** Turns the Vessel into a free-roaming Vault Champion; every change is gated on {@code greed_champion}. */
@Mixin(value = TheVesselEntity.class, remap = false)
public abstract class MixinVaultChampionVessel {

    @Shadow public abstract float getDamageMultiplier();

    @Shadow public abstract boolean isAttacking();

    @Shadow private int attackCooldown;

    @Shadow private int teleportCooldown;

    @Shadow private int weaponClapGraceTicks;

    @Shadow private int postSpellChaseTicks;

    /** Substitutes the Champion's quarry for the source entity, so pets and summons pass {@code hurt}'s player gate. */
    @ModifyExpressionValue(method = "hurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"),
            remap = true)
    private Entity woldsvaults$championAcceptsLivingAttackers(Entity original) {
        TheVesselEntity self = (TheVesselEntity) (Object) this;
        if (original instanceof Player || !(original instanceof LivingEntity)
                || !VaultChampion.isChampion(self)) {
            return original;
        }
        return self.getTarget() instanceof Player quarry ? quarry : original;
    }

    /** Champions take damage from anything with a living attacker; sources with no attacker stay refused. */
    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true, remap = true)
    private void woldsvaults$championTakesAllDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !VaultChampion.isChampion((TheVesselEntity) (Object) this)) {
            return;
        }
        if (source.getEntity() instanceof LivingEntity) {
            cir.setReturnValue(false);
        }
    }

    /** Rescales every Vessel spell's flat damage by how far this Champion's attack damage sits above baseline. */
    @Inject(method = "getScaledDamage", at = @At("RETURN"), cancellable = true)
    private void woldsvaults$championScalesSpellDamage(float baseDamage, CallbackInfoReturnable<Float> cir) {
        TheVesselEntity self = (TheVesselEntity) (Object) this;
        if (!VaultChampion.isChampion(self)) {
            return;
        }
        GreedChampionConfig.Scaling scaling = VaultChampion.config().getScaling();
        if (scaling.baseAttackDamage <= 0.0D) {
            return;
        }
        AttributeInstance attack = self.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return;
        }
        double factor = attack.getValue() / scaling.baseAttackDamage;
        cir.setReturnValue((float) (baseDamage * this.getDamageMultiplier() * factor));
    }

    /** Drops the "target is on the ground" condition from the teleport gate; every other gate still applies. */
    @Inject(method = "canStartTeleport", at = @At("RETURN"), cancellable = true)
    private void woldsvaults$championIgnoresGroundGate(CallbackInfoReturnable<Boolean> cir) {
        TheVesselEntity self = (TheVesselEntity) (Object) this;
        if (cir.getReturnValue() || !VaultChampion.isChampion(self)) {
            return;
        }
        if (this.isAttacking() || this.teleportCooldown > 0 || this.weaponClapGraceTicks > 0
                || this.postSpellChaseTicks > 0) {
            return;
        }
        if (self.getTarget() != null) {
            cir.setReturnValue(true);
        }
    }

    /** Attack-speed scaling, applied as extra cooldown decay. The teleport cooldown is left alone. */
    @Inject(method = "tick", at = @At("TAIL"), remap = true)
    private void woldsvaults$championAttackSpeed(CallbackInfo ci) {
        TheVesselEntity self = (TheVesselEntity) (Object) this;
        if (self.level.isClientSide || self.isDormant() || !VaultChampion.isChampion(self)) {
            return;
        }
        GreedChampionConfig.Rank stats = VaultChampion.rankStats(self);
        if (stats == null || stats.attackSpeedMultiplier <= 1.0D) {
            return;
        }
        int extra = (int) Math.floor(stats.attackSpeedMultiplier) - 1;
        if (extra <= 0) {
            return;
        }
        if (!this.isAttacking() && this.attackCooldown > 0) {
            this.attackCooldown = Math.max(0, this.attackCooldown - extra);
        }
        if (this.postSpellChaseTicks > 0) {
            this.postSpellChaseTicks = Math.max(0, this.postSpellChaseTicks - extra);
        }
        if (VaultChampion.config().getScaling().scaleTeleportCooldown && this.teleportCooldown > 0) {
            this.teleportCooldown = Math.max(0, this.teleportCooldown - extra);
        }
    }

    /** A flat second hit that nothing reduces; it can still be dodged or blocked on its own roll. */
    @Inject(method = "doHurtTarget", at = @At("RETURN"), remap = true)
    private void woldsvaults$championTrueDamage(net.minecraft.world.entity.Entity target,
                                                CallbackInfoReturnable<Boolean> cir) {
        TheVesselEntity self = (TheVesselEntity) (Object) this;
        if (!cir.getReturnValue() || !(target instanceof Player player) || !VaultChampion.isChampion(self)) {
            return;
        }
        VaultTrueDamage.deal(self, player, VaultChampion.config().getTrueDamage().champion);
    }
}

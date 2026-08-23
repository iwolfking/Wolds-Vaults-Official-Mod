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

/**
 * The three entity-level changes that turn the greed trial's Vessel into a free-roaming Vault
 * Champion. All of them are gated on the {@code greed_champion} tag, so the trial boss - which is the
 * same class - keeps every one of its arena behaviours untouched.
 *
 * <p>The one that matters most is {@code getScaledDamage}. Only the Vessel's melee reads its attack
 * damage attribute; every one of its eleven spells passes a hardcoded float through this method
 * instead, so a Champion authored at a quarter of a million melee damage would still be throwing
 * ten-damage lightning bolts. Scaling those constants by how far the attribute has been raised above
 * the Vessel's own baseline keeps the whole kit proportional through a single hook.</p>
 */
@Mixin(value = TheVesselEntity.class, remap = false)
public abstract class MixinVaultChampionVessel {

    @Shadow public abstract float getDamageMultiplier();

    @Shadow public abstract boolean isAttacking();

    @Shadow private int attackCooldown;

    @Shadow private int teleportCooldown;

    @Shadow private int weaponClapGraceTicks;

    @Shadow private int postSpellChaseTicks;

    /**
     * The Vessel's own {@code hurt} refuses everything that is not a player's hand before the
     * invulnerability check below is ever consulted, so relaxing that alone would change nothing.
     * This is what actually lets a pet, an Eternal or a summon reach a Champion.
     *
     * <p>It works by widening the one test that gate is made of. {@code hurt} calls
     * {@code getEntity()} exactly once, at bytecode offset 24, and the value it returns is consumed
     * immediately by {@code instanceof Player} and by nothing else - the damage itself is applied
     * from the original {@code source} further down. So substituting the Champion's own quarry when
     * the real attacker is some other living thing opens the gate without touching attribution,
     * credit or the amount. Verified against the shipped 3.21.6 bytecode, and it is the reason this
     * is a substitution rather than an override.
     *
     * <p>Sources with no living attacker at all are left refused: that is suffocation, cacti and
     * unowned magic, none of which should chip a boss down while the player stands back.</p>
     */
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

    /**
     * Champions take damage from anything with a living attacker behind it - pets, Eternals, summons,
     * player damage-over-time - rather than only from a player's own hand. Sources with no attacker at
     * all stay refused: that is suffocation, cacti and unowned magic, none of which should be able to
     * chip a boss down while the player stands back.
     */
    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true, remap = true)
    private void woldsvaults$championTakesAllDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !VaultChampion.isChampion((TheVesselEntity) (Object) this)) {
            return;
        }
        if (source.getEntity() instanceof LivingEntity) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Rescales the flat damage constants every Vessel spell is built on, in proportion to how far this
     * Champion's attack damage sits above the entity's authored baseline.
     */
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

    /**
     * Drops the "target is standing on the ground" condition from the teleport gate. It is the reason
     * an airborne player can walk away from the Vessel in the open world: the arena fight patches that
     * by stripping Elytra and Shadow Cloak from its objective, which a Champion in an ordinary vault
     * has no equivalent of. Every other gate - the cooldown, the clap grace period, the post-spell
     * chase window - is re-checked here and still applies.
     */
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

    /**
     * Attack-speed scaling, done as extra cooldown decay rather than by shortening the animations.
     * The Vessel has no attack-speed attribute at all - its cadence is nothing but these counters, and
     * most of its melee time is spent waiting one of them out rather than swinging - so draining them
     * faster is the whole of the effect, and the animations keep playing at their authored length.
     *
     * <p>The teleport cooldown is left alone unless the pack asks for it. Scaling it would put the
     * grab-and-stun on a very short cycle at the top ranks, on top of the lightning uptime and the
     * leash, which is more loss of control per second than the fight can carry.</p>
     */
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

    /**
     * The flat second hit that nothing reduces, though it can still be dodged or blocked on its own
     * roll. It rides every melee connection the Champion makes, including the ones inside the blitz
     * combo and the runaway clap, because all of them route through here; a swing that missed or
     * was dodged returns {@code false} and lands no follow-up.
     */
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

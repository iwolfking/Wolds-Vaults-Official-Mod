package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.spi.AbstractFireballAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.lib.VolleyFireball;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.api.util.WoldEtchingHelper;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileSteering;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.effect.mobeffects.PercentBurnEffect;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = VaultFireball.class, remap = false)
public abstract class MixinVaultFireball extends AbstractArrow implements IWoldAbilityEnhancements, VolleyFireball {
    @Unique
    private static final double WOLDSVAULTS$MITOSIS_SPEED = 0.5;

    @Unique
    private static final double WOLDSVAULTS$MITOSIS_FACE_OFFSET = 0.1;

    protected MixinVaultFireball(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    public abstract void explode(Vec3 pos);

    @Shadow
    private int bounceCount;

    @Shadow
    public abstract Player getThrower();

    @Shadow
    public abstract VaultFireball createBouncingFireball(Level level, LivingEntity thrower, int bounceCount);

    @Shadow
    public abstract VaultFireball.FireballType getFireballType();

    @Unique
    private boolean woldsvaults$split;

    @Unique
    private boolean woldsvaults$mitosisChild;

    @Unique
    private boolean woldsvaults$keepAlive;

    @Unique
    private boolean woldsvaults$burst;

    @Unique
    private long woldsvaults$burstTick;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;explode(Lnet/minecraft/world/phys/Vec3;)V", remap = false), remap = true)
    private void wrapWithElementalFlags(VaultFireball instance, Vec3 pos, Operation<Void> original) {
        ElementHelper.runWithElements(getElementTypes(), () -> original.call(instance, pos));
    }

    /**
     * Volley contacts. An orbiting Volley ball treats every entity or block contact as a bounce that keeps it in its
     * orbit: it explodes (at most once per the table's gap), splits on its first contact, and spawns no bounce copy.
     * Outside an orbit, the first entity a not-yet-bounced ball hits splits it.
     */
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$volleyContact(HitResult result, CallbackInfo ci) {
        if (this.level.isClientSide() || getFireballType() != VaultFireball.FireballType.BOUNCING || result.getType() == HitResult.Type.MISS) {
            return;
        }
        if (ProjectileSteering.isOrbiting(this)) {
            woldsvaults$split(result);
            woldsvaults$orbitBurst(result);
            ci.cancel();
        } else if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity) {
            woldsvaults$split(result);
        }
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;createBouncingFireball(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Liskallia/vault/entity/entity/VaultFireball;", ordinal = 0, remap = false), remap = true)
    private void fireVolleyBouncesExplode(HitResult result, CallbackInfo ci) {
        woldsvaults$split(result);
        explode(result.getLocation());
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", remap = true))
    private void woldsvaults$keepOrbitingVolley(VaultFireball instance, Entity.RemovalReason reason, Operation<Void> original) {
        if (!this.woldsvaults$keepAlive) {
            original.call(instance, reason);
        }
    }

    /** An orbiting Volley ball flies through blocks instead of lodging in them, so a wall it touches does not end its orbit. */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/VoxelShape;isEmpty()Z", remap = true), remap = true)
    private boolean woldsvaults$orbitThroughBlocks(VoxelShape shape, Operation<Boolean> original) {
        return original.call(shape) || (getFireballType() == VaultFireball.FireballType.BOUNCING && ProjectileSteering.isOrbiting(this));
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;getFireballType()Liskallia/vault/entity/entity/VaultFireball$FireballType;"))
    private VaultFireball.FireballType woldsvaults$mitosisChildExplodesAsVolley(VaultFireball instance, Operation<VaultFireball.FireballType> original) {
        return this.woldsvaults$mitosisChild ? VaultFireball.FireballType.BOUNCING : original.call(instance);
    }

    /**
     * Volley Mitosis: the first bounce or entity hit of a Volley ball that has not bounced yet spawns the etching's
     * number of children at once, aimed into the half-space around the hit face (block) or back along the ball's motion
     * (entity). Bounce copies never split; each Shotgun copy and Echo recast is its own ball and splits on its own.
     */
    @Unique
    private void woldsvaults$split(HitResult result) {
        if (this.woldsvaults$split || this.bounceCount != 0) {
            return;
        }
        this.woldsvaults$split = true;
        Player thrower = getThrower();
        if (thrower == null) {
            return;
        }
        int count = EtchingHelper.getEtchings(thrower, ModEtchingGearAttributes.FIREBALL_VOLLEY_MITOSIS).stream().findFirst()
                .map(VaultGearAttributeInstance::getValue).orElse(0);
        if (count <= 0) {
            return;
        }
        Vec3 axis;
        Vec3 origin;
        if (result instanceof BlockHitResult block) {
            axis = Vec3.atLowerCornerOf(block.getDirection().getNormal());
            origin = block.getLocation().add(axis.scale(WOLDSVAULTS$MITOSIS_FACE_OFFSET));
        } else {
            Vec3 motion = getDeltaMovement();
            axis = motion.lengthSqr() > 1.0E-8 ? motion.normalize().scale(-1.0) : new Vec3(0, 1, 0);
            origin = position();
        }
        for (int i = 0; i < count; i++) {
            woldsvaults$spawnMitosisChild(thrower, origin, woldsvaults$towards(axis));
        }
    }

    @Unique
    private Vec3 woldsvaults$towards(Vec3 axis) {
        double y = this.random.nextDouble() * 2.0 - 1.0;
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double ring = Math.sqrt(1.0 - y * y);
        Vec3 direction = new Vec3(ring * Math.cos(angle), y, ring * Math.sin(angle));
        double dot = direction.dot(axis);
        return dot < 0 ? direction.subtract(axis.scale(2.0 * dot)) : direction;
    }

    @Unique
    private void woldsvaults$spawnMitosisChild(Player thrower, Vec3 origin, Vec3 direction) {
        VaultFireball ball = this.createBouncingFireball(this.level, thrower, 0);
        ball.setType(VaultFireball.FireballType.BASE);
        ball.pickup = AbstractArrow.Pickup.DISALLOWED;
        ((IWoldAbilityEnhancements) ball).setTierLevel(getTierLevel());
        ((IWoldAbilityEnhancements) ball).setElementTypes(new ArrayList<>(getElementTypes()));
        ((VolleyFireball) ball).woldsvaults$makeMitosisChild();
        ProjectileGlyphTags.leaveOrbit(ball);
        ball.setPos(origin);
        ball.setDeltaMovement(direction.scale(WOLDSVAULTS$MITOSIS_SPEED));
        float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance()));
        ball.setYRot(yaw);
        ball.setXRot(pitch);
        ball.yRotO = yaw;
        ball.xRotO = pitch;
        this.level.addFreshEntity(ball);
    }

    @Unique
    private void woldsvaults$orbitBurst(HitResult result) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            return;
        }
        long now = this.level.getGameTime();
        if (this.woldsvaults$burst && now - this.woldsvaults$burstTick < config.tuning.orbitVolleyExplosionGapTicks) {
            return;
        }
        this.woldsvaults$burst = true;
        this.woldsvaults$burstTick = now;
        Vec3 pos = result.getType() == HitResult.Type.BLOCK ? result.getLocation() : position();
        this.woldsvaults$keepAlive = true;
        try {
            explode(pos);
        } finally {
            this.woldsvaults$keepAlive = false;
        }
    }

    @Override
    public void woldsvaults$makeMitosisChild() {
        this.woldsvaults$mitosisChild = true;
        this.woldsvaults$split = true;
    }

    @Unique
    private VaultFireball.FireballType woldsvaults$abilityType() {
        return this.woldsvaults$mitosisChild ? VaultFireball.FireballType.BOUNCING : getFireballType();
    }

    @WrapOperation(method = "getRadius", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/ability/effect/spi/AbstractFireballAbility;getRadius()F"))
    private float getGreedBallRadius(AbstractFireballAbility instance, Operation<Float> original, @Local(name = "player") ServerPlayer player) {
        if(WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.FIREBALL_GREEDBALL)) {
            return 3.0F;
        }

        return original.call(instance);
    }

    @Inject(method = "lambda$onHit$6", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = true))
    private static void applyBurnEffectToFireshot(LivingEntity living, DamageSource damageSource, float attackDamage, CallbackInfo ci) {
        if(damageSource.getEntity() instanceof LivingEntity attacker) {
            PercentBurnEffect.applyPercentBurn(living, attacker,220, attackDamage);
        }
    }

    @Inject(method = "getDamage", at = @At("HEAD"), cancellable = true)
    private void computeScepterFireballDamage(CallbackInfoReturnable<Float> cir) {
        if (this.getOwner() instanceof ServerPlayer serverPlayer) {
            if (getTierLevel() >= 0) {
                InstantAbility ability = AbilityHelper.getAbilityRefFromConfig(woldsvaults$abilityType().getAbilityName(), getTierLevel()).orElse(null);
                if(ability instanceof AbstractFireballAbility abstractFireballAbility) {
                    cir.setReturnValue(abstractFireballAbility.getAbilityPower(serverPlayer));
                }
            }
        }
    }

    @Inject(method = "getRadius", at = @At("HEAD"), cancellable = true)
    private void computerScepterFireballRadius(CallbackInfoReturnable<Float> cir) {
        if (this.getOwner() instanceof ServerPlayer) {
            if (getTierLevel() >= 0) {
                InstantAbility ability = AbilityHelper.getAbilityRefFromConfig(woldsvaults$abilityType().getAbilityName(), getTierLevel()).orElse(null);
                if(ability instanceof AbstractFireballAbility abstractFireballAbility) {
                    cir.setReturnValue(abstractFireballAbility.getRadius());
                }
            }
        }
    }

    @Unique
    private int woldsvaults$tierLevel = -1;

    @Unique
    private List<ElementalType> elementalTypes = new ArrayList<>();

    @Override
    public void setTierLevel(int level) {
        woldsvaults$tierLevel = level;
    }

    @Override
    public int getTierLevel() {
        return woldsvaults$tierLevel;
    }

    @Override
    public void setElementTypes(List<ElementalType> elementalTypes) {
        this.elementalTypes = elementalTypes;
    }

    @Override
    public List<ElementalType> getElementTypes() {
        return elementalTypes;
    }
}

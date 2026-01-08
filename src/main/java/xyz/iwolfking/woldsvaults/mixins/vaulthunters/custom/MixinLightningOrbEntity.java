package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.entity.LightningOrbEntity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(value = LightningOrbEntity.class, remap = false)
public abstract class MixinLightningOrbEntity extends ThrowableItemProjectile {
//    @Unique
//    private static float baseRadius = 4.0F;
//    @Shadow
//    @Final
//    private static EntityDataAccessor<Float> DAMAGE_MIN;
//    @Shadow
//    @Final
//    private static EntityDataAccessor<Float> DAMAGE_MAX;
//    @Unique
//    private int ticks = 0;
//    @Shadow
//    @Final
//    private Map<Integer, Integer> hitCooldowns;
//
    public MixinLightningOrbEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
//
//    @Inject(method = "tick", at = @At("TAIL"))
//    private void triggerRadialShocks(CallbackInfo ci, @Local Player player) {
//        if(ticks % 10 == 0) {
//            Skill ability = ModConfigs.ABILITIES.getAbilityById("Chain_Lightning_Orbs").orElse(null);
//            if(ability == null) {
//                return;
//            }
//
//            float adjustedRadius = AreaOfEffectHelper.adjustAreaOfEffect(player, ability, baseRadius);
//
//            AABB box = new AABB(
//                    this.getX() - adjustedRadius, this.getY() - adjustedRadius, this.getZ() - adjustedRadius,
//                    this.getX() + adjustedRadius, this.getY() + adjustedRadius, this.getZ() + adjustedRadius
//            );
//
//
//            List<LivingEntity> entities = player.level.getEntitiesOfClass(
//                    LivingEntity.class,
//                    box,
//                    e -> !(e instanceof Player)
//            );
//
//            if (entities.isEmpty()) {
//                return;
//            }
//
//            LivingEntity target = entities.get(new Random().nextInt(entities.size()));
//
//            float abilityPower = AbilityPowerHelper.getAbilityPower(player);
//            float damage = abilityPower * this.getModifiedLightningDamage(player, Mth.nextFloat(player.getRandom(), this.entityData.get(DAMAGE_MIN), (Float)this.entityData.get(DAMAGE_MAX)));
//            this.applyLightningDamage(target, damage, player);
//            Entity bolt = ModEntities.SMITE_ABILITY_BOLT.create(target.level);
//            if (bolt != null) {
//                bolt.moveTo(target.position());
//                target.level.addFreshEntity(bolt);
//            }
//
//            target.level.playSound(null, target, ModSounds.LIGHTING_BOLT, SoundSource.PLAYERS, 1.0F, 1.0F);
//
//            this.hitCooldowns.put(target.getId(), 10);
//        }
//
//        ticks++;
//
//    }
//
//    @Shadow
//    protected abstract float getModifiedLightningDamage(Player player, float baseDamage);
//
//    @Shadow
//    protected abstract void applyLightningDamage(LivingEntity target, float damage, Player player);
}

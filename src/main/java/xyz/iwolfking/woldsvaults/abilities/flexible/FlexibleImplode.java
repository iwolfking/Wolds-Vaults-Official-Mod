package xyz.iwolfking.woldsvaults.abilities.flexible;

import com.mojang.math.Vector3f;
import iskallia.vault.client.particles.SphericalParticleOptions;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.init.ModParticles;
import iskallia.vault.init.ModSounds;
import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.skill.ability.effect.ImplodeAbility;
import iskallia.vault.util.AABBHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FlexibleImplode extends ImplodeAbility {

    public float cast(Player player, LivingEntity target, ImplodeAbility ability) {
        return this.cast(player, target, ability, ((ManaPlayer) player).getMana(), 1.0F);
    }

    /**
     * Casts Implode around {@code target}, dealing {@code mana} x percentManaDealt x falloff x
     * {@code damageScale} to each enemy in range. Returns the number of enemies in range.
     */
    public float cast(Player player, LivingEntity target, ImplodeAbility ability, float mana, float damageScale) {
        Vec3 pos = target.position();

        float radius = ability.getRadius(player);
        List<LivingEntity> targetEntities = player.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat().range(radius).selector((entity) -> !(entity instanceof Player) && !(entity instanceof EternalEntity)), player, AABBHelper.create(pos, radius));;
        DamageSource damageSource = DamageSource.playerAttack(player);

        for (LivingEntity entity : targetEntities) {
            if (!entity.isInvulnerableTo(damageSource)) {
                float damageModifier = this.getDamageModifier(ability.getRadius(target), target.distanceTo(entity));
                float damage = mana * ability.getPercentManaDealt() * damageModifier * damageScale;
                ActiveFlags.IS_AOE_ATTACKING.runIfNotSet(() -> entity.hurt(damageSource, damage));
            }
        }
        ((ServerPlayer) player).getLevel().sendParticles(new SphericalParticleOptions(ModParticles.IMPLODE.get(), ability.getRadius(player), new Vector3f(0.0F, 1.0F, 1.0F)), target.getX(), target.getY(), target.getZ(), 400, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
        player.level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.MANA_SHIELD, SoundSource.PLAYERS, 0.2F, 0.2F);
        player.playNotifySound(ModSounds.MANA_SHIELD, SoundSource.PLAYERS, 0.2F, 0.2F);

        return targetEntities.size();
    }
}

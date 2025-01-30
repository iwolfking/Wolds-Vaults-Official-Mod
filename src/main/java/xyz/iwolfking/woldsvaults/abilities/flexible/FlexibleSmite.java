package xyz.iwolfking.woldsvaults.abilities.flexible;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.SmiteAbility;
import iskallia.vault.util.calc.AbilityPowerHelper;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jline.utils.Log;

public class FlexibleSmite extends SmiteAbility {
    public float cast(Player player, Entity target, SmiteAbility ability) {
        DamageSource srcPlayerAttack = DamageSource.playerAttack(player);
        SmiteBolt smiteBolt = iskallia.vault.init.ModEntities.SMITE_ABILITY_BOLT.create(player.level);
        if (smiteBolt != null) {
            smiteBolt.setColor(ability.getColor());
            smiteBolt.moveTo(target.position());
            player.level.addFreshEntity(smiteBolt);
        }

        ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> ability.getFlag().runIfNotSet(() -> {
            double damage = (double)(AbilityPowerHelper.getAbilityPower(player) * ability.getAbilityPowerPercent());
            target.hurt(srcPlayerAttack, (float)damage);
            Log.info("Smite Damage: " + damage);
        }));
        if (this.getFlag() == ActiveFlags.IS_SMITE_BASE_ATTACKING) {
            player.level.playSound((Player)null, target.getX(), target.getY(), target.getZ(), ModSounds.SMITE_BOLT, SoundSource.PLAYERS, 1.0F, 1.0F + Mth.randomBetween(((LivingEntity) target).getRandom(), -0.2F, 0.2F));
        } else {
            player.level.playSound((Player)null, target.getX(), target.getY(), target.getZ(), ModSounds.SMITE_BOLT, SoundSource.PLAYERS, 0.7F, 1.5F + Mth.randomBetween(((LivingEntity) target).getRandom(), -0.2F, 0.2F));
        }

        return ability.getAdditionalManaPerBolt();
    }
}

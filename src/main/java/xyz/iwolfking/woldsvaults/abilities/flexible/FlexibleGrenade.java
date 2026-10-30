package xyz.iwolfking.woldsvaults.abilities.flexible;

import iskallia.vault.entity.entity.VaultGrenade;
import iskallia.vault.skill.ability.effect.GrenadeAbility;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class FlexibleGrenade extends GrenadeAbility {

    public void cast(Player player, GrenadeAbility ability, float xRotation, float yRotation) {
        if (!(player.level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        float damage = getAttackDamageForAbility(serverPlayer, ability);
        float radius = ability.getRadius(serverPlayer);
        int duration = ability.getDuration();

        VaultGrenade grenade = new VaultGrenade(serverLevel, serverPlayer, damage, radius, duration);
        grenade.setSticky(ability.isSticky());
        grenade.shootFromRotation(serverPlayer, xRotation, yRotation, 0.0F, ability.getThrowPower(), 0.0F);

        serverLevel.addFreshEntity(grenade);
        serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private float getAttackDamageForAbility(ServerPlayer player, GrenadeAbility ability) {
        float base = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float attackDamagePercentageIncrease = (Float) AttributeSnapshotHelper.getInstance()
                .getSnapshot(player)
                .getAttributeValue(ModGearAttributes.DAMAGE_INCREASE, VaultGearAttributeTypeMerger.floatSum());

        base += base * attackDamagePercentageIncrease;
        float damage = base * ability.getPercentAttackDamageDealt(); // Crucial: use passed ability instance!
        return damage * PlayerDamageHelper.getDamageMultiplier(player, true, false);
    }

    public void cast(Player player, GrenadeAbility ability) {
        cast(player, ability, player.getXRot(), player.getYRot());
    }
}
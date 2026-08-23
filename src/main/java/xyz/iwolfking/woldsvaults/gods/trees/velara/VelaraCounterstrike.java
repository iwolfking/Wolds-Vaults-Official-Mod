package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityDamageBlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Counterstrike: a chance to hit back for the player's attack damage after blocking or dodging
 * melee. The retaliation is a raw damage instance, not a weapon swing.
 */
public final class VelaraCounterstrike {
    private VelaraCounterstrike() {
    }

    static void register() {
        CommonEvents.ENTITY_DAMAGE_BLOCK.blockSucceeded()
                .register(VelaraStatBus.LISTENER_REF, data -> onBlock(data));
    }

    private static void onBlock(EntityDamageBlockEvent.Data data) {
        if (data.getAttacked() instanceof ServerPlayer player) {
            onDefended(player, data.getDamageSource());
        }
    }

    /** Rolls and resolves a counterstrike. Safe to call for any avoided hit; filtered here. */
    public static void onDefended(LivingEntity defender, DamageSource source) {
        if (!(defender instanceof ServerPlayer player) || VelaraActiveFlags.IS_COUNTERSTRIKING.isSet()) {
            return;
        }
        if (!VelaraNodes.isActive(player, VelaraNodes.COUNTERSTRIKE) || !isMelee(source)) {
            return;
        }
        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity target) || target == player || !target.isAlive()) {
            return;
        }
        if (player.getRandom().nextFloat() > VelaraValues.counterstrikeChance()) {
            return;
        }
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (damage <= 0.0F) {
            return;
        }
        VelaraActiveFlags.IS_COUNTERSTRIKING.runWithFlag(() -> target.hurt(DamageSource.playerAttack(player), damage));
    }

    private static boolean isMelee(DamageSource source) {
        if (source.isProjectile() || source.isExplosion() || source.isMagic() || source.isBypassInvul() || source.isFall()) {
            return false;
        }
        return source.getDirectEntity() instanceof LivingEntity;
    }
}

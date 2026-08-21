package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityDamageBlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Counterstrike: a 50% chance to hit back after blocking or dodging a melee strike.
 *
 * <p>The block half needs no patching -  the addon's own {@code blockAttack} overwrite already
 * fires {@code CommonEvents.ENTITY_DAMAGE_BLOCK}, so this registers on that event. The dodge half
 * has no event at all; the addon's dodge site calls {@link #onDefended} directly.
 *
 * <p>The retaliation is a raw damage instance rather than a simulated weapon swing, matching the
 * sheet's "raw damage (ability)" classification. That deliberately keeps it out of cleave,
 * chaining, echoing and fatal-strike re-entry, which is the class of bug the addon already had to
 * fight for proc fangs.
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

    /**
     * Rolls and resolves a counterstrike after the player avoided {@code source}. Safe to call for
     * any avoided hit -  non-melee sources and players without the node are filtered here.
     */
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

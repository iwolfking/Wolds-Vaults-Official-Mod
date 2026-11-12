package xyz.iwolfking.woldsvaults.modifiers.gear.riptide;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.network.message.RiptideFxMessage;

import java.util.Comparator;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The riptide shock cone: every tick of a dash, mobs inside a cone from {@code shockConeAhead} blocks ahead of the
 * player to a {@code shockConeRadius} radius {@code shockConeBehind} blocks behind are shocked once per dash for
 * {@link ModGearAttributes#RIPTIDE_SHOCK_CONE} times the dash's first contact final damage, and struck by the storm's
 * lightning. A dash that has not touched a mob when the cone first finds one lands a contact on the nearest cone
 * target ({@link RiptideDash#forceContact}), so the cone always has a contact to draw from; if that contact deals no
 * damage the waiting mobs take the player's previous dash value. Area of effect does not scale the cone.
 */
public class RiptideShockCone implements RiptideDash.Listener {
    @Override
    public void onTick(ServerPlayer player, RiptideDash.Dash dash) {
        float multiplier = multiplier(player);
        if (multiplier <= 0.0F) {
            return;
        }
        GearSetConfig.Riptide tuning = GearSetHelper.config().getRiptide();
        Vec3 origin = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 axis = dash.getDirection();
        double reach = Math.max(tuning.shockConeRadius, Math.max(tuning.shockConeAhead, tuning.shockConeBehind)) + 1.0;
        List<LivingEntity> candidates = player.level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(reach),
                entity -> isTarget(player, entity) && inCone(entity, origin, axis, tuning));
        if (dash.getReferenceDamage() <= 0.0F && !candidates.isEmpty()) {
            candidates.stream().min(Comparator.comparingDouble(player::distanceToSqr))
                    .ifPresent(nearest -> RiptideDash.forceContact(player, nearest));
        }
        for (LivingEntity target : candidates) {
            if (!dash.markShocked(target)) {
                continue;
            }
            if (dash.getReferenceDamage() > 0.0F) {
                hit(player, dash, target, multiplier, dash.getReferenceDamage());
            } else {
                dash.getPendingShock().add(target);
            }
        }
        RiptideFxMessage.send(player, RiptideFxMessage.cone(player, origin, axis, tuning.shockConeAhead, tuning.shockConeBehind, tuning.shockConeRadius));
    }

    @Override
    public void onContact(ServerPlayer player, RiptideDash.Dash dash, LivingEntity target, float finalDamage) {
        flush(player, dash, dash.getReferenceDamage());
    }

    @Override
    public void onEnd(ServerPlayer player, RiptideDash.Dash dash) {
        if (dash.getPendingShock().isEmpty()) {
            return;
        }
        float reference = RiptideDash.lastReference(player);
        if (reference <= 0.0F) {
            WoldLogger.debug("Riptide shock cone: {} mobs shocked by {} before any riptide contact; no damage to draw from, skipped.", dash.getPendingShock().size(), player.getScoreboardName());
            dash.getPendingShock().clear();
            return;
        }
        flush(player, dash, reference);
    }

    private static void flush(ServerPlayer player, RiptideDash.Dash dash, float reference) {
        if (reference <= 0.0F || dash.getPendingShock().isEmpty()) {
            return;
        }
        float multiplier = multiplier(player);
        dash.getPendingShock().forEach(target -> hit(player, dash, target, multiplier, reference));
        dash.getPendingShock().clear();
    }

    private static float multiplier(ServerPlayer player) {
        return RiptideDash.snapshot(player).getAttributeValue(ModGearAttributes.RIPTIDE_SHOCK_CONE, VaultGearAttributeTypeMerger.floatSum());
    }

    /** The cone's hit on {@code target} for its multiplier times the dash's contact damage, and the storm's lightning. */
    private static void hit(ServerPlayer player, RiptideDash.Dash dash, LivingEntity target, float multiplier, float reference) {
        RiptideDerivedHits.queue(player, target, multiplier * reference);
        RiptideFxMessage.send(player, RiptideFxMessage.burst(player, target, RiptideStorm.isChilled(target)));
        RiptideStorm.strike(player, dash, target, reference);
    }

    /** A mob riptide may hit: not the player, another player, an eternal, a pet or an ally. */
    public static boolean isTarget(Player player, LivingEntity entity) {
        return entity != player && entity.isAlive() && !(entity instanceof Player)
                && !(entity instanceof EternalEntity) && !(entity instanceof PetEntity) && !player.isAlliedTo(entity);
    }

    /** Inside the cone whose apex is {@code shockConeAhead} blocks along the axis and whose base has radius {@code shockConeRadius}. */
    static boolean inCone(LivingEntity entity, Vec3 origin, Vec3 axis, GearSetConfig.Riptide tuning) {
        Vec3 offset = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0).subtract(origin);
        double along = offset.dot(axis);
        if (along > tuning.shockConeAhead || along < -tuning.shockConeBehind) {
            return false;
        }
        double length = tuning.shockConeAhead + tuning.shockConeBehind;
        double allowed = tuning.shockConeRadius * (tuning.shockConeAhead - along) / length + entity.getBbWidth() * 0.5;
        return offset.subtract(axis.scale(along)).length() <= allowed;
    }
}

package xyz.iwolfking.woldsvaults.modifiers.gear;

import com.mojang.math.Vector3f;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.entity.entity.necromancy.NecromancyGolemEntity;
import iskallia.vault.entity.entity.necromancy.NecromancySkeletonEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.DerivedHit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves Gravity combo pulls at the end of the server tick. Better Combat fires one attack per target hit by a
 * swing, so several gravity combos can complete on the same swing; their target positions are collected per player
 * during the tick and resolved together into one pull toward their average position. Every hostile within the largest
 * rolled radius of that point is pulled in with a velocity impulse, ignoring knockback resistance. Players, allies,
 * pets, eternals, necromancy minions, owned mobs and bosses are never pulled.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GravityComboPulls {
    private static final double PULL_PER_BLOCK = 0.38D;
    private static final double MAX_HORIZONTAL_SPEED = 2.2D;
    private static final double LIFT = 0.22D;
    private static final double MIN_DISTANCE = 0.75D;

    private static final Map<ServerPlayer, Pending> PENDING = new LinkedHashMap<>();

    private GravityComboPulls() {
    }

    private static final class Pending {
        private final List<Vec3> points = new ArrayList<>();
        private float radius;
        private int color;
    }

    /** Records a completed gravity combo on a target at {@code point}, pulled in at the end of this tick. */
    public static void record(ServerPlayer player, Vec3 point, float radius, int color) {
        Pending pending = PENDING.computeIfAbsent(player, p -> new Pending());
        pending.points.add(point);
        pending.radius = Math.max(pending.radius, radius);
        pending.color = color;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        List<Map.Entry<ServerPlayer, Pending>> batch = new ArrayList<>(PENDING.entrySet());
        PENDING.clear();
        for (Map.Entry<ServerPlayer, Pending> entry : batch) {
            ServerPlayer player = entry.getKey();
            if (player.isRemoved() || !(player.level instanceof ServerLevel level)) {
                continue;
            }
            pull(level, player, entry.getValue());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PENDING.clear();
    }

    private static void pull(ServerLevel level, ServerPlayer player, Pending pending) {
        Vec3 centre = Vec3.ZERO;
        for (Vec3 point : pending.points) {
            centre = centre.add(point);
        }
        centre = centre.scale(1.0D / pending.points.size());
        double radius = pending.radius;
        AABB area = new AABB(centre, centre).inflate(radius);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, mob -> canPull(player, mob))) {
            Vec3 offset = centre.subtract(mob.position());
            double distance = offset.length();
            if (distance > radius || distance < MIN_DISTANCE) {
                continue;
            }
            Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
            double speed = Math.min(PULL_PER_BLOCK * horizontal.length(), MAX_HORIZONTAL_SPEED);
            Vec3 push = horizontal.lengthSqr() > 1.0E-6D ? horizontal.normalize().scale(speed) : Vec3.ZERO;
            double lift = LIFT + Math.max(0.0D, Math.min(offset.y * 0.12D, 0.4D));
            mob.setDeltaMovement(push.x, lift, push.z);
            mob.hasImpulse = true;
            mob.hurtMarked = true;
        }
        spawnFeedback(level, centre, pending.color);
    }

    private static boolean canPull(ServerPlayer player, LivingEntity mob) {
        if (!mob.isAlive() || mob.isSpectator() || !(mob instanceof Enemy)) {
            return false;
        }
        if (mob instanceof Player || mob instanceof ArmorStand || DerivedHit.isBoss(mob)) {
            return false;
        }
        if (mob instanceof PetEntity || mob instanceof EternalEntity
                || mob instanceof NecromancySkeletonEntity || mob instanceof NecromancyGolemEntity) {
            return false;
        }
        if (mob instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return false;
        }
        return !mob.isAlliedTo(player);
    }

    private static void spawnFeedback(ServerLevel level, Vec3 centre, int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        level.sendParticles(new DustParticleOptions(new Vector3f(red, green, blue), 1.6F),
                centre.x, centre.y + 0.75D, centre.z, 40, 1.2D, 0.6D, 1.2D, 0.02D);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.6F);
    }
}

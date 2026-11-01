package xyz.iwolfking.woldsvaults.modifiers.gear.lightning;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.LightningLashFxMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Arc Lash's bolts (Lightning Fiend 6-piece): arcLashBolts lightning bolts thrown out in a fan from the player's hand
 * across arcLashConeAngle either side of where they look, each flying straight at arcLashSpeed blocks a tick until it
 * has gone arcLashRange or meets a wall. A bolt strikes every enemy it passes through and calls a channeling strike
 * (vanilla lightning) down on it for arcLashStrikeDamage of a channeling strike's damage; one throw hits each enemy
 * only once, however many of its bolts pass through it. The flights are simulated here on the server; clients draw
 * the same flights from {@link LightningLashFxMessage}.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ArcLashBolts {
    private static final double HIT_RADIUS = 1.0;
    private static final List<Bolt> BOLTS = new ArrayList<>();

    private static final class Bolt {
        final ServerPlayer owner;
        final ServerLevel level;
        final Vec3 origin;
        final Vec3 direction;
        final double length;
        final double speed;
        final float damage;
        final float strikeDamage;
        final Set<Integer> struck;
        int age;

        Bolt(ServerPlayer owner, Vec3 origin, Vec3 direction, double length, double speed, float damage, float strikeDamage, Set<Integer> struck) {
            this.owner = owner;
            this.level = owner.getLevel();
            this.origin = origin;
            this.direction = direction;
            this.length = length;
            this.speed = speed;
            this.damage = damage;
            this.strikeDamage = strikeDamage;
            this.struck = struck;
        }
    }

    private ArcLashBolts() {
    }

    static void fire(ServerPlayer player, Vec3 from, Vec3 look, GearSetConfig.Lightning tuning, float damage, float strikeDamage) {
        ServerLevel level = player.getLevel();
        int count = Math.max(1, tuning.arcLashBolts);
        double speed = Math.max(0.1, tuning.arcLashSpeed);
        Set<Integer> struck = new HashSet<>();
        List<Vec3> directions = new ArrayList<>(count);
        List<Float> lengths = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float turn = count == 1 ? 0.0F : (float) Math.toRadians(tuning.arcLashConeAngle * (2.0F * i / (count - 1) - 1.0F));
            Vec3 direction = look.yRot(turn).normalize();
            Vec3 end = from.add(direction.scale(tuning.arcLashRange));
            BlockHitResult wall = level.clip(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            double length = wall.getType() == HitResult.Type.MISS ? tuning.arcLashRange : wall.getLocation().distanceTo(from);
            BOLTS.add(new Bolt(player, from, direction, length, speed, damage, strikeDamage, struck));
            directions.add(direction);
            lengths.add((float) length);
        }
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new LightningLashFxMessage(from, directions, lengths, (float) speed, level.getRandom().nextLong()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !BOLTS.isEmpty()) {
            BOLTS.removeIf(ArcLashBolts::fly);
        }
    }

    private static boolean fly(Bolt bolt) {
        if (bolt.owner.isRemoved() || bolt.owner.getLevel() != bolt.level) {
            return true;
        }
        Vec3 a = bolt.origin.add(bolt.direction.scale(bolt.speed * bolt.age));
        bolt.age++;
        double travelled = Math.min(bolt.length, bolt.speed * bolt.age);
        Vec3 b = bolt.origin.add(bolt.direction.scale(travelled));
        for (Mob mob : bolt.level.getEntitiesOfClass(Mob.class, new AABB(a, b).inflate(HIT_RADIUS + 1.0), LightningFiend::isEnemy)) {
            AABB box = mob.getBoundingBox().inflate(HIT_RADIUS);
            if (!bolt.struck.contains(mob.getId()) && (box.contains(a) || box.clip(a, b).isPresent())) {
                bolt.struck.add(mob.getId());
                LightningFiend.hit(bolt.owner, mob, bolt.damage);
                LightningFiend.onArcLashContact(bolt.owner, mob);
                if (bolt.strikeDamage > 0.0F) {
                    LightningFiend.strike(bolt.owner, mob, bolt.strikeDamage);
                }
                Vec3 center = mob.getBoundingBox().getCenter();
                bolt.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, mob.getBbWidth() * 0.4, mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.4, 0.3);
            }
        }
        if (travelled >= bolt.length) {
            bolt.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, b.x, b.y, b.z, 6, 0.2, 0.2, 0.2, 0.2);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        BOLTS.clear();
    }
}

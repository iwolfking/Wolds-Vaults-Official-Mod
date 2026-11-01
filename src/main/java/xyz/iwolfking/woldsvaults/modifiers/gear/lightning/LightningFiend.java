package xyz.iwolfking.woldsvaults.modifiers.gear.lightning;

import iskallia.vault.VaultMod;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDerivedHits;
import xyz.iwolfking.woldsvaults.network.message.LightningArcFxMessage;
import xyz.iwolfking.woldsvaults.network.message.LightningDashFxMessage;
import xyz.iwolfking.woldsvaults.network.message.LightningFormMessage;
import xyz.iwolfking.woldsvaults.network.message.LightningMarkMessage;
import xyz.iwolfking.woldsvaults.network.message.LightningStrikeFxMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Lightning Fiend set's lightning, driven from the thrown trident: channeling strikes that hit harder and chain
 * to nearby enemies ({@link ModGearAttributes#CHANNELING_DAMAGE_MORE}, {@link ModGearAttributes#CHANNELING_CHAIN}),
 * a strike on every enemy a piercing throw passes through ({@link ModGearAttributes#THROWN_TRIDENT_PIERCE}), and the
 * lightning form ({@link ModGearAttributes#LIGHTNING_DASH}): after a throw the thrower becomes lightning for a while,
 * and each tap of a movement key dashes them that way, striking every enemy along the path; left click throws a fan
 * of spinning lightning bolts out in front of them, right click marks an enemy as a lightning rod that dashes lock on to,
 * and middle click spends what's left of the form on one giant strike. The client sends the taps and clicks and
 * carries its player along each dash; the server checks the form and the cooldowns, plans the path, picks the
 * targets and deals the hits. Every hit starts from the player's attack damage when the trident was thrown and goes
 * through {@link RiptideDerivedHits#queueScaled} under {@link WoldActiveFlags#IS_LIGHTNING_FIEND_ATTACKING}: damage
 * increase, damage talents (Hexbreaker included) and lucky hits apply, on-hit procs do not. The bolts are visual only.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class LightningFiend {
    /** Dash directions, relative to where the player faces, in the order the client sends them. */
    public enum Dash { FORWARD, LEFT, RIGHT, BACK, UP, DOWN }

    private static final Map<UUID, Form> FORMS = new HashMap<>();

    private static final class Form {
        long endTick;
        float charge;
        float attackDamage;
        float damage;
        long readyTick;
        Travel travel;
        long lashReadyTick;
        long rodReadyTick;
        int markId = -1;
    }

    private static final class Travel {
        final ServerLevel level;
        final List<Vec3> path;
        final int ticks;
        final float width;
        final int cooldownTicks;
        final float markMultiplier;
        final Set<Integer> struck = new HashSet<>();
        int age;
        int swept;

        Travel(ServerLevel level, List<Vec3> path, int ticks, GearSetConfig.Lightning tuning) {
            this.level = level;
            this.path = path;
            this.ticks = ticks;
            this.width = tuning.dashWidth;
            this.cooldownTicks = tuning.dashCooldownTicks;
            this.markMultiplier = tuning.rodDamageMultiplier;
        }
    }

    private LightningFiend() {
    }

    private static AttributeSnapshot snapshot(LivingEntity entity) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(entity);
    }

    /** The multiplier on channeling strike damage: 1 plus the wearer's more-channeling-damage. */
    public static float channelingMultiplier(LivingEntity owner) {
        return 1.0F + snapshot(owner).getAttributeValue(ModGearAttributes.CHANNELING_DAMAGE_MORE, VaultGearAttributeTypeMerger.floatSum());
    }

    /** How many enemies the owner's thrown trident passes through before it stops. */
    public static int pierce(LivingEntity owner) {
        return snapshot(owner).getAttributeValue(ModGearAttributes.THROWN_TRIDENT_PIERCE, VaultGearAttributeTypeMerger.intSum());
    }

    /** The player's attack damage, the unscaled base every Lightning Fiend hit starts from. */
    public static float attackDamage(LivingEntity owner) {
        return (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    /** A bolt on the target and a Lightning Fiend hit for the damage. */
    public static void strike(ServerPlayer attacker, LivingEntity target, float damage) {
        visualBolt(attacker.getLevel(), target.position());
        hit(attacker, target, damage);
    }

    /** Queues a Lightning Fiend hit: unscaled damage that the damage pipeline scales, with on-hit procs off. */
    static void hit(ServerPlayer attacker, LivingEntity target, float damage) {
        RiptideDerivedHits.queueScaled(attacker, target, damage, WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING);
    }

    /** Jumps from the struck enemy to the nearest one not struck yet, again and again, up to the chain count. */
    public static void chain(ServerPlayer attacker, LivingEntity from, float damage) {
        GearSetConfig config = GearSetHelper.config();
        int jumps = snapshot(attacker).getAttributeValue(ModGearAttributes.CHANNELING_CHAIN, VaultGearAttributeTypeMerger.intSum());
        if (config == null || jumps <= 0 || damage <= 0.0F) {
            return;
        }
        float range = config.getLightning().chainRange;
        Set<Integer> struck = new HashSet<>();
        struck.add(from.getId());
        LivingEntity current = from;
        for (int i = 0; i < jumps; i++) {
            Vec3 at = current.position();
            Mob next = nearestEnemy(attacker, at, range, struck);
            if (next == null) {
                return;
            }
            struck.add(next.getId());
            strike(attacker, next, damage);
            current = next;
        }
    }

    /**
     * A thrown vault trident, called while it is still in the thrower's hand: with the lightning dash grant, the
     * thrower becomes lightning, its hits based on the attack damage they had with the trident in hand.
     */
    public static void onThrow(ServerPlayer player) {
        GearSetConfig config = GearSetHelper.config();
        float multiplier = snapshot(player).getAttributeValue(ModGearAttributes.LIGHTNING_DASH, VaultGearAttributeTypeMerger.floatSum());
        if (config == null || multiplier <= 0.0F) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        Form form = FORMS.computeIfAbsent(player.getUUID(), id -> new Form());
        form.endTick = player.server.getTickCount() + tuning.formDurationTicks;
        form.charge = 0.0F;
        form.attackDamage = attackDamage(player);
        form.damage = form.attackDamage * channelingMultiplier(player) * multiplier;
        ModNetwork.sendToClient(new LightningFormMessage(tuning.formDurationTicks, tuning.dashCooldownTicks, tuning.arcLashCooldownTicks, tuning.rodCooldownTicks), player);
        visualBolt(player.getLevel(), player.position());
    }

    /**
     * Thunderstrike charge for a lightning hit on {@code target}: {@code amount}, times chargeBossMultiplier on a boss
     * (the the_vault:bosses entity group), up to chargeMax. Nothing outside the form.
     */
    public static void addCharge(ServerPlayer player, LivingEntity target, float amount) {
        Form form = FORMS.get(player.getUUID());
        GearSetConfig config = GearSetHelper.config();
        if (form == null || config == null || amount <= 0.0F) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        float boss = ModConfigs.ENTITY_GROUPS.isInGroup(VaultMod.id("bosses"), target) ? tuning.chargeBossMultiplier : 1.0F;
        form.charge = Math.min(tuning.chargeMax, form.charge + amount * boss);
    }

    /** Charge for the throw's hit on {@code target} (chargePerThrowHit). */
    public static void onThrowHit(ServerPlayer player, LivingEntity target) {
        GearSetConfig config = GearSetHelper.config();
        if (config != null) {
            addCharge(player, target, config.getLightning().chargePerThrowHit);
        }
    }

    /** Charge for an Arc Lash bolt's contact with {@code target} (chargePerArcLash). */
    public static void onArcLashContact(ServerPlayer player, LivingEntity target) {
        GearSetConfig config = GearSetHelper.config();
        if (config != null) {
            addCharge(player, target, config.getLightning().chargePerArcLash);
        }
    }

    /** The form's resistance cap bonus for {@code entity} (formResistanceCapBonus while lightning, else 0). */
    public static float resistanceCapBonus(LivingEntity entity) {
        GearSetConfig config = GearSetHelper.config();
        if (config == null || !(entity instanceof ServerPlayer player) || !inForm(player)) {
            return 0.0F;
        }
        return config.getLightning().formResistanceCapBonus;
    }

    public static boolean inForm(ServerPlayer player) {
        Form form = FORMS.get(player.getUUID());
        return form != null && (form.travel != null || player.server.getTickCount() < form.endTick);
    }

    /** Whether the player is lightning right now: the server's form, or on the client what the server told it. */
    public static boolean isLightning(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return inForm(serverPlayer);
        }
        return player.level.isClientSide && Boolean.TRUE.equals(DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm.isLightning(player)));
    }

    /**
     * A dash the client asked for, relative to the given yaw: up to dashDistance blocks that way (stopping at walls,
     * ceilings and floors, stepping up single blocks), crossed as lightning over dashTravelTicks. The client carries
     * its player along the path, everyone near draws the bolt, and the server strikes each enemy within dashWidth of
     * the path as the bolt reaches it. A dash towards the player's Lightning Rod target (within rodLockAngle of it)
     * locks on instead: straight through the target, however far, and on past it for rodOvershoot of a dash.
     */
    public static void dash(ServerPlayer player, Dash direction, float yaw) {
        GearSetConfig config = GearSetHelper.config();
        Form form = FORMS.get(player.getUUID());
        long now = player.server.getTickCount();
        if (config == null || form == null || form.travel != null || now >= form.endTick || now < form.readyTick
                || !player.isAlive() || player.isSpectator() || player.isPassenger()) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        Vec3 start = player.position();
        Vec3 heading = heading(yaw, direction);
        float distance = tuning.dashDistance;
        LivingEntity marked = marked(player, form, tuning);
        if (marked != null) {
            Vec3 aim = marked.getBoundingBox().getCenter().subtract(0.0, player.getBbHeight() * 0.5, 0.0).subtract(start);
            if (Math.abs(aim.y) < 1.0) {
                aim = new Vec3(aim.x, 0.0, aim.z);
            }
            if (aim.lengthSqr() > 1.0E-4 && aim.normalize().dot(heading) >= Math.cos(Math.toRadians(tuning.rodLockAngle))) {
                heading = aim.normalize();
                distance = (float) aim.length() + tuning.dashDistance * tuning.rodOvershoot;
            }
        }
        List<Vec3> path = path(player, start, heading, distance);
        if (path.get(path.size() - 1).distanceToSqr(start) < 0.25) {
            return;
        }
        int ticks = Math.max(1, tuning.dashTravelTicks);
        ServerLevel level = player.getLevel();
        form.travel = new Travel(level, path, ticks, tuning);
        player.fallDistance = 0.0F;

        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new LightningDashFxMessage(player.getId(), path, ticks, level.getRandom().nextLong()));
        Vec3 body = start.add(0.0, player.getBbHeight() * 0.5, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, body.x, body.y, body.z, 30, 0.3, 0.6, 0.3, 0.4);
        level.playSound(null, start.x, start.y, start.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6F, 1.8F);
    }

    /** Where a dash has got to at the given progress (0 to 1); each step of the path takes the same time. */
    public static Vec3 along(List<Vec3> path, float progress) {
        if (path.size() < 2) {
            return path.get(0);
        }
        float at = Math.max(0.0F, Math.min(1.0F, progress)) * (path.size() - 1);
        int i = Math.min((int) at, path.size() - 2);
        return path.get(i).lerp(path.get(i + 1), at - i);
    }

    private static void travelTick(ServerPlayer player, Form form) {
        Travel travel = form.travel;
        if (!player.isAlive() || player.getLevel() != travel.level) {
            form.travel = null;
            return;
        }
        travel.age++;
        player.fallDistance = 0.0F;
        int last = travel.path.size() - 1;
        strikeAlong(player, form, travel, Math.min(last, Math.round(last * (float) travel.age / travel.ticks)));
        if (travel.age < travel.ticks) {
            return;
        }
        form.travel = null;
        form.readyTick = player.server.getTickCount() + travel.cooldownTicks;
        ServerLevel level = player.getLevel();
        Vec3 end = travel.path.get(last);
        Vec3 body = end.add(0.0, player.getBbHeight() * 0.5, 0.0);
        level.sendParticles(ParticleTypes.FLASH, body.x, body.y, body.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, body.x, body.y, body.z, 30, 0.3, 0.6, 0.3, 0.4);
        level.playSound(null, end.x, end.y, end.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.5F);
    }

    private static void strikeAlong(ServerPlayer player, Form form, Travel travel, int reach) {
        if (reach <= travel.swept) {
            return;
        }
        Vec3 body = new Vec3(0.0, player.getBbHeight() * 0.5, 0.0);
        List<Vec3> points = travel.path.subList(travel.swept, reach + 1);
        AABB box = new AABB(points.get(0), points.get(points.size() - 1)).move(body).inflate(travel.width + 2.0);
        for (Mob mob : enemies(player, box)) {
            if (travel.struck.contains(mob.getId())) {
                continue;
            }
            Vec3 center = mob.getBoundingBox().getCenter();
            for (int i = 0; i < points.size() - 1; i++) {
                if (distanceToSegment(center, points.get(i).add(body), points.get(i + 1).add(body)) <= travel.width + mob.getBbWidth() * 0.5) {
                    travel.struck.add(mob.getId());
                    boolean marked = mob.getId() == form.markId;
                    hit(player, mob, marked ? form.damage * travel.markMultiplier : form.damage);
                    GearSetConfig config = GearSetHelper.config();
                    if (config != null) {
                        addCharge(player, mob, marked ? config.getLightning().chargePerRod : config.getLightning().chargePerDash);
                    }
                    player.getLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 15, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.5, mob.getBbWidth() * 0.5, 0.5);
                    break;
                }
            }
        }
        travel.swept = reach;
    }

    /**
     * Arc Lash (left click): throws a fan of spinning lightning bolts out from the player's hand across a cone in
     * front of them, each striking the enemies it passes through for arcLashDamage of the throw's attack damage and calling
     * a channeling strike down on them for arcLashStrikeDamage of a channeling strike's damage (see
     * {@link ArcLashBolts}). At most once per arcLashCooldownTicks.
     */
    public static void arcLash(ServerPlayer player, Vec3 look) {
        GearSetConfig config = GearSetHelper.config();
        Form form = FORMS.get(player.getUUID());
        long now = player.server.getTickCount();
        if (config == null || form == null || now >= form.endTick || now < form.lashReadyTick || !player.isAlive() || player.isSpectator()) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        form.lashReadyTick = now + tuning.arcLashCooldownTicks;
        float channeling = form.attackDamage * channelingMultiplier(player);
        ArcLashBolts.fire(player, hand(player, look), look, tuning, form.attackDamage * tuning.arcLashDamage, channeling * tuning.arcLashStrikeDamage);
        ServerLevel level = player.getLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4F, 1.6F + level.getRandom().nextFloat() * 0.4F);
    }

    /**
     * Lightning Rod (right click): marks the enemy the player aims at within rodRange, which glows while marked. Dashes
     * towards it lock on (see {@link #dash}) and strike it for rodDamageMultiplier of the dash damage. One mark at a
     * time, kept until the target dies or the form ends. A miss does nothing; a mark costs rodCooldownTicks.
     */
    public static void lightningRod(ServerPlayer player, int targetId, Vec3 look) {
        GearSetConfig config = GearSetHelper.config();
        Form form = FORMS.get(player.getUUID());
        long now = player.server.getTickCount();
        if (config == null || form == null || now >= form.endTick || now < form.rodReadyTick || !player.isAlive() || player.isSpectator()) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        Mob target = aimed(player, targetId, look, tuning.rodRange, 10.0);
        if (target == null) {
            return;
        }
        form.rodReadyTick = now + tuning.rodCooldownTicks;
        ServerLevel level = player.getLevel();
        if (form.markId != target.getId() && level.getEntity(form.markId) instanceof LivingEntity old) {
            old.removeEffect(MobEffects.GLOWING);
        }
        form.markId = target.getId();
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, (int) (form.endTick - now), 0, false, false));
        ModNetwork.sendToClient(new LightningMarkMessage(target.getId()), player);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new LightningArcFxMessage(List.of(hand(player, look), target.getBoundingBox().getCenter()), level.getRandom().nextLong()));
        visualBolt(level, target.position());
    }

    /**
     * Thunderstrike (middle click): spends the form's charge on one giant bolt down onto the player, striking every
     * enemy within a radius growing linearly from thunderBaseRadius to thunderMaxRadius with charge, for
     * thunderBaseDamage of the throw's attack damage times 1 + (thunderMaxMultiplier - 1) x (charge / chargeMax)^2.
     * Ends the form.
     */
    public static void thunderstrike(ServerPlayer player) {
        GearSetConfig config = GearSetHelper.config();
        Form form = FORMS.get(player.getUUID());
        long now = player.server.getTickCount();
        if (config == null || form == null || form.travel != null || now >= form.endTick || !player.isAlive() || player.isSpectator()) {
            return;
        }
        GearSetConfig.Lightning tuning = config.getLightning();
        float filled = tuning.chargeMax > 0.0F ? form.charge / tuning.chargeMax : 0.0F;
        float damage = form.attackDamage * tuning.thunderBaseDamage * (1.0F + (tuning.thunderMaxMultiplier - 1.0F) * filled * filled);
        float radius = tuning.thunderBaseRadius + (tuning.thunderMaxRadius - tuning.thunderBaseRadius) * filled;
        ServerLevel level = player.getLevel();
        Vec3 at = player.position();
        for (Mob mob : enemies(player, new AABB(at, at).inflate(radius, radius * 0.5, radius))) {
            double dx = mob.getX() - at.x, dz = mob.getZ() - at.z;
            if (dx * dx + dz * dz <= radius * radius) {
                hit(player, mob, damage);
                Vec3 center = mob.getBoundingBox().getCenter();
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 12, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.5, mob.getBbWidth() * 0.5, 0.5);
            }
        }
        endForm(player, form, true);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new LightningStrikeFxMessage(at, radius, level.getRandom().nextLong()));
        level.sendParticles(ParticleTypes.FLASH, at.x, at.y + 1.0, at.z, 3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y + 0.5, at.z, 150, radius * 0.4, 0.5, radius * 0.4, 1.0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0F, 0.6F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.5F);
    }

    /** Ends the form: the mark's glow goes, and when {@code notify} the client hears its form is over. */
    private static void endForm(ServerPlayer player, Form form, boolean notify) {
        if (form.markId >= 0 && player.getLevel().getEntity(form.markId) instanceof LivingEntity marked) {
            marked.removeEffect(MobEffects.GLOWING);
        }
        FORMS.remove(player.getUUID());
        if (notify) {
            ModNetwork.sendToClient(new LightningFormMessage(0, 0, 0, 0), player);
        }
    }

    private static void endForm(ServerPlayer player, boolean notify) {
        Form form = FORMS.get(player.getUUID());
        if (form != null) {
            endForm(player, form, notify);
        }
    }

    @Nullable
    private static LivingEntity marked(ServerPlayer player, Form form, GearSetConfig.Lightning tuning) {
        if (form.markId < 0) {
            return null;
        }
        if (player.getLevel().getEntity(form.markId) instanceof LivingEntity living && living.isAlive()) {
            return living.distanceTo(player) <= tuning.rodRange ? living : null;
        }
        form.markId = -1;
        ModNetwork.sendToClient(new LightningMarkMessage(-1), player);
        return null;
    }

    @Nullable
    private static Mob aimed(ServerPlayer player, int targetId, Vec3 look, float range, double angle) {
        Vec3 eye = player.getEyePosition();
        if (player.getLevel().getEntity(targetId) instanceof Mob mob && isEnemy(mob)
                && mob.getBoundingBox().getCenter().distanceTo(eye) <= range + mob.getBbWidth() && player.hasLineOfSight(mob)) {
            return mob;
        }
        Mob best = null;
        double bestDot = Math.cos(Math.toRadians(angle));
        for (Mob mob : enemies(player, new AABB(eye, eye).inflate(range))) {
            Vec3 to = mob.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance > range || distance < 1.0E-4) {
                continue;
            }
            double dot = to.scale(1.0 / distance).dot(look);
            if (dot >= bestDot && player.hasLineOfSight(mob)) {
                best = mob;
                bestDot = dot;
            }
        }
        return best;
    }

    private static Vec3 hand(ServerPlayer player, Vec3 look) {
        double yaw = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));
        return player.getEyePosition().add(0.0, -0.4, 0.0).add(right.scale(0.35)).add(look.scale(0.3));
    }

    private static Vec3 heading(float yaw, Dash direction) {
        double rad = Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(rad), 0.0, Math.cos(rad));
        Vec3 left = new Vec3(Math.cos(rad), 0.0, Math.sin(rad));
        return switch (direction) {
            case FORWARD -> forward;
            case BACK -> forward.scale(-1.0);
            case LEFT -> left;
            case RIGHT -> left.scale(-1.0);
            case UP -> new Vec3(0.0, 1.0, 0.0);
            case DOWN -> new Vec3(0.0, -1.0, 0.0);
        };
    }

    private static List<Vec3> path(ServerPlayer player, Vec3 start, Vec3 heading, float distance) {
        Vec3 step = heading.scale(0.25);
        Vec3 flat = new Vec3(step.x, 0.0, step.z);
        boolean sideways = flat.lengthSqr() > 1.0E-8;
        boolean level = Math.abs(step.y) < 1.0E-8;
        List<Vec3> path = new ArrayList<>();
        path.add(start);
        Vec3 pos = start;
        for (int i = 0; i < Math.round(distance * 4.0F); i++) {
            Vec3 next = pos.add(step);
            if (fits(player, next)) {
                pos = next;
            } else if (sideways && !level && fits(player, pos.add(flat))) {
                pos = pos.add(flat);
            } else if (sideways && fits(player, pos.add(0.0, 1.0, 0.0)) && fits(player, pos.add(flat).add(0.0, 1.0, 0.0))) {
                pos = pos.add(flat).add(0.0, 1.0, 0.0);
            } else {
                break;
            }
            path.add(pos);
        }
        return path;
    }

    private static boolean fits(ServerPlayer player, Vec3 at) {
        return player.getLevel().noCollision(player, player.getBoundingBox().move(at.subtract(player.position())));
    }

    private static double distanceToSegment(Vec3 point, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double t = ab.lengthSqr() < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(1.0, point.subtract(a).dot(ab) / ab.lengthSqr()));
        return point.distanceTo(a.add(ab.scale(t)));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || FORMS.isEmpty()) {
            return;
        }
        Form form = FORMS.get(player.getUUID());
        if (form == null) {
            return;
        }
        if (form.travel != null) {
            travelTick(player, form);
            return;
        }
        if (player.server.getTickCount() >= form.endTick) {
            endForm(player, form, true);
            return;
        }
        if (player.tickCount % 2 == 0) {
            player.getLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(), 6, 0.35, 0.8, 0.35, 0.15);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player && inForm(player)) {
            event.setDistance(0.0F);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            endForm(player, true);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            endForm(player, true);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            endForm(player, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            endForm(player, false);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FORMS.clear();
    }

    private static List<Mob> enemies(ServerPlayer attacker, AABB box) {
        return attacker.getLevel().getEntitiesOfClass(Mob.class, box, LightningFiend::isEnemy);
    }

    static boolean isEnemy(Mob mob) {
        return mob.isAlive() && !(mob instanceof EternalEntity) && !(mob instanceof PetEntity);
    }

    @Nullable
    private static Mob nearestEnemy(ServerPlayer attacker, Vec3 at, float range, Set<Integer> skip) {
        return enemies(attacker, new AABB(at, at).inflate(range)).stream()
                .filter(mob -> !skip.contains(mob.getId()) && mob.distanceToSqr(at) <= range * range)
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(at)))
                .orElse(null);
    }

    private static void visualBolt(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(at.x, at.y, at.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }
}

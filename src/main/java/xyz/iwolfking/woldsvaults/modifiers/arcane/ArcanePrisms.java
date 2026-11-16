package xyz.iwolfking.woldsvaults.modifiers.arcane;

import com.mojang.math.Vector3f;
import iskallia.vault.entity.IPlayerAttackIgnore;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.ArcanePrismAbility;
import iskallia.vault.util.calc.AbilityPowerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ArcanePrismAbilityInvoker;
import xyz.iwolfking.woldsvaults.network.message.ArcanePrismMessage;
import xyz.iwolfking.woldsvaults.network.message.ArcanePrismSeedMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Arcane: Prism, reworked for everyone who casts it.
 * <ul>
 * <li>The seed: casting launches an arcane seed from the caster's hand that homes in on the mob Vault Hunters picked
 * (or the block, with the anchor etching). The Prism is placed where the seed lands, on the mob wherever it has moved
 * to by then.</li>
 * <li>The beams: in place of Vault Hunters' pulses, the pyramid's four beams (eight with the split etching, the extra
 * four at half strength) shine for the whole time it lasts, sweeping round as it turns. Every {@link #BEAM_TICKS}
 * ticks, everything a beam touched since the last hit is hurt, with no cooldown, ignoring the brief immunity after a
 * hit. Each hit is worth that share of a pulse, so a mob held in the beams (the one the Prism is anchored on) takes
 * what the pulses gave it, and anything swept takes damage for as long as a beam is on it. Vault Hunters' target cap
 * still holds: at most maxTargets enemies hit by each kind of beam at a time. The beams don't knock mobs back, and a
 * mob's hurt sound plays at most every {@link #HURT_SOUND_TICKS} ticks.</li>
 * <li>Bent light: a beam that something bends ({@link BeamOptics}) stops there and carries on along the path it is
 * given. That light hurts what it touches in the same way, for the share the bender sets, on top of the beams
 * themselves.</li>
 * </ul>
 * The server works out the beams a few times a tick, so a fast sweep can't skip over anything. Clients trace the same
 * paths every frame to draw them.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ArcanePrisms {
    // how often the beams hurt what they touch
    private static final int BEAM_TICKS = 2;
    // checks of the beams a tick, between hits
    private static final int SUBSTEPS = 4;
    private static final int HURT_SOUND_TICKS = 10;
    private static final float SPLIT_SHARE = 0.5F;
    // the seed's flight: blocks a tick, and its shortest and longest time in the air
    private static final double SEED_SPEED = 1.8;
    private static final int SEED_MIN_TICKS = 3;
    private static final int SEED_MAX_TICKS = 16;
    private static final Vector3f HIT_SPARK = new Vector3f(1.0F, 0.95F, 0.75F);

    private static final List<Seed> SEEDS = new ArrayList<>();
    private static final List<Prism> PRISMS = new ArrayList<>();
    // set while a landed seed has Vault Hunters place its Prism, so that placing goes ahead
    private static boolean landing;
    // the mob a beam is hurting right now (the server thread only), whether to hush its hurt sound, and the share of a
    // full hit that this one counts for
    @Nullable
    private static LivingEntity beamTarget;
    private static boolean hushed;
    private static float hitShare = 1.0F;

    private static final class Seed {
        final ArcanePrismAbility ability;
        final ServerPlayer caster;
        final ServerLevel level;
        @Nullable
        final LivingEntity target;
        final Vec3 forward;
        final float damageMultiplier;
        final long lands;
        // where it lands: the target's middle, or where that was when it last lived
        Vec3 at;

        Seed(ArcanePrismAbility ability, ServerPlayer caster, @Nullable LivingEntity target, Vec3 at, Vec3 forward, float damageMultiplier, long lands) {
            this.ability = ability;
            this.caster = caster;
            this.level = caster.getLevel();
            this.target = target;
            this.at = at;
            this.forward = forward;
            this.damageMultiplier = damageMultiplier;
            this.lands = lands;
        }
    }

    // heard: when each mob's hurt sound last played, by entity id
    private record Prism(ServerLevel level, ServerPlayer caster, Vec3 position, double yaw, long created, int duration, int interval, float reach,
                         float percent, float beamWidth, int maxTargets, boolean split, BeamOptics.Lens lens, Map<Integer, Long> heard) {
    }

    private ArcanePrisms() {
    }

    // Vault Hunters is about to place a Prism at position: launch the seed that carries it there instead, unless this
    // is a seed landing. Returns whether the placing should wait for the seed.
    public static boolean launch(ArcanePrismAbility ability, ServerPlayer caster, Vec3 position, Vec3 forward, float damageMultiplier) {
        if (landing) {
            return false;
        }
        ServerLevel level = caster.getLevel();
        // Vault Hunters aims the Prism at the middle of the mob it picked
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, new AABB(position, position).inflate(0.05),
                        entity -> entity != caster && entity.getBoundingBox().getCenter().distanceToSqr(position) < 1.0E-4)
                .stream().findFirst().orElse(null);
        Vec3 hand = castingHand(caster);
        double distance = hand.distanceTo(position);
        int flight = Mth.clamp(Mth.ceil(distance / SEED_SPEED), SEED_MIN_TICKS, SEED_MAX_TICKS);
        long now = level.getGameTime();
        SEEDS.add(new Seed(ability, caster, target, position, forward, damageMultiplier, now + flight));
        ModNetwork.sendNear(level, hand.lerp(position, 0.5), 96.0 + distance / 2.0,
                new ArcanePrismSeedMessage(hand, target == null ? -1 : target.getId(), position, now, flight));
        level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.7F);
        level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.5F, 1.6F);
        return true;
    }

    // A Prism placed (by a landed seed): remember it, to turn its beams and hurt what they touch for as long as it
    // lasts, and have nearby clients draw it.
    public static void created(ServerPlayer caster, Vec3 position, Vec3 forward, int durationTicks, int intervalTicks, float radius,
                               float percentAbilityPower, float beamWidth, int maxTargets) {
        ServerLevel level = caster.getLevel();
        long now = level.getGameTime();
        double yaw = Math.atan2(forward.z, forward.x);
        float reach = BeamOptics.bender().prismReach(caster, radius);
        boolean split = EtchingHelper.getEtchings(caster, EtchingGearAttributes.PRISM_SPLIT).stream()
                .findFirst().map(VaultGearAttributeInstance::getValue).orElse(false);
        BeamOptics.Lens lens = BeamOptics.bender().lens();
        PRISMS.add(new Prism(level, caster, position, yaw, now, Math.max(1, durationTicks), Math.max(1, intervalTicks), reach, percentAbilityPower,
                beamWidth, maxTargets, split, lens, new HashMap<>()));
        ModNetwork.sendNear(level, position, 96.0 + reach, new ArcanePrismMessage(position, (float) yaw, now, durationTicks, reach, split, (float) lens.bentLength(), (float) lens.bentRadius()));
    }

    // The share of a full hit the damage being dealt now counts for: less for a beam's frequent hits.
    public static float hitShare() {
        return beamTarget == null ? 1.0F : hitShare;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tickSeeds();
        PRISMS.removeIf(prism -> prism.caster().isRemoved() || !prism.caster().isAlive() || prism.caster().level != prism.level()
                || prism.level().getGameTime() - prism.created() >= prism.duration());
        for (Prism prism : List.copyOf(PRISMS)) {
            tick(prism);
        }
    }

    // a beam's hits don't knock the mob about
    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (beamTarget != null && event.getEntityLiving() == beamTarget) {
            event.setCanceled(true);
        }
    }

    // nor make it cry out every hit (its death cry still plays)
    @SubscribeEvent
    public static void onSound(PlaySoundAtEntityEvent event) {
        if (beamTarget != null && hushed && !beamTarget.isDeadOrDying() && event.getCategory() == beamTarget.getSoundSource()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SEEDS.clear();
        PRISMS.clear();
        beamTarget = null;
    }

    private static void tickSeeds() {
        List<Seed> landed = new ArrayList<>();
        for (Iterator<Seed> it = SEEDS.iterator(); it.hasNext(); ) {
            Seed seed = it.next();
            if (seed.caster.isRemoved() || !seed.caster.isAlive() || seed.caster.level != seed.level) {
                it.remove();
                continue;
            }
            if (seed.target != null && seed.target.isAlive() && seed.target.level == seed.level) {
                seed.at = seed.target.getBoundingBox().getCenter();
            }
            if (seed.level.getGameTime() >= seed.lands) {
                it.remove();
                landed.add(seed);
            }
        }
        for (Seed seed : landed) {
            landing = true;
            try {
                ((ArcanePrismAbilityInvoker) seed.ability).woldsvaults$createPrism(seed.caster, seed.at, seed.forward, seed.damageMultiplier);
            } finally {
                landing = false;
            }
        }
    }

    // Every BEAM_TICKS ticks: every beam's path (bent by anything in its way), checked a few times a tick since the last hit
    // against every enemy near enough, then a hit on each one any of them touched, once for each kind of beam (main,
    // split, bent light).
    private static void tick(Prism prism) {
        long now = prism.level().getGameTime();
        long age = now - prism.created();
        if (age < 0 || age % BEAM_TICKS != 0) {
            return;
        }
        float bentShare = BeamOptics.bender().bentDamage();
        BeamOptics.Lens lens = prism.lens();
        BeamOptics.Paths paths = BeamOptics.bender().near(prism.level(), prism.position(), prism.reach(), lens);
        double area = prism.reach() + (paths == BeamOptics.STRAIGHT ? 0.0 : lens.bentLength()) + 2.0;
        DamageSource source = DamageSource.playerAttack(prism.caster());
        List<LivingEntity> targets = prism.level().getEntitiesOfClass(LivingEntity.class, new AABB(prism.position(), prism.position()).inflate(area),
                entity -> canHurt(prism.caster(), entity, source));
        if (targets.isEmpty()) {
            return;
        }
        // what each kind of beam touched since the last hit, and the strongest share it touched it with
        Map<LivingEntity, Float> main = new LinkedHashMap<>();
        Map<LivingEntity, Float> split = new LinkedHashMap<>();
        Map<LivingEntity, Float> bent = new LinkedHashMap<>();
        int beams = prism.split() ? 8 : 4;
        int checks = age == 0 ? 1 : BEAM_TICKS * SUBSTEPS;
        for (int step = 1; step <= checks; step++) {
            float time = age == 0 ? 0.0F : age - BEAM_TICKS + (float) step / SUBSTEPS;
            double spin = prism.yaw() + Math.toRadians(ArcanePrismMessage.SPIN_DEGREES_PER_TICK) * time;
            for (int k = 0; k < beams; k++) {
                boolean splitBeam = prism.split() && k % 2 == 1;
                double a = spin + k * Math.PI * 2.0 / beams;
                Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
                List<BeamOptics.Ray> rays = paths.trace(prism.position(), dir, prism.reach(), prism.caster());
                float share = splitBeam ? SPLIT_SHARE : 1.0F;
                for (BeamOptics.Ray ray : rays) {
                    if (ray.inside()) {
                        continue;
                    }
                    boolean isBent = ray.bentBy() != null;
                    Map<LivingEntity, Float> touched = isBent ? bent : splitBeam ? split : main;
                    for (LivingEntity target : targets) {
                        // the target cap counts enemies the beams themselves hit at once
                        boolean overCap = !isBent && !touched.containsKey(target) && prism.maxTargets() > 0 && touched.size() >= prism.maxTargets();
                        if (!overCap && touched.getOrDefault(target, 0.0F) < share && BeamOptics.touches(target, ray.from(), ray.to(), prism.beamWidth())) {
                            touched.put(target, share);
                        }
                    }
                }
            }
        }
        float hit = AbilityPowerHelper.getAbilityPower(prism.caster()) * prism.percent() * BEAM_TICKS / prism.interval();
        float fraction = (float) BEAM_TICKS / prism.interval();
        main.forEach((target, share) -> hurt(prism, target, source, hit * share, fraction, now));
        split.forEach((target, share) -> hurt(prism, target, source, hit * share, fraction, now));
        bent.forEach((target, share) -> hurt(prism, target, source, hit * share * bentShare, fraction, now));
    }

    // whom Vault Hunters' own Prism hurts (leaving out other players, which it only hurts where the vault allows it)
    private static boolean canHurt(ServerPlayer caster, LivingEntity target, DamageSource source) {
        return target instanceof Mob mob && mob.isAlive() && !(mob instanceof EternalEntity) && !(mob instanceof PetEntity)
                && !IPlayerAttackIgnore.isIgnoredByPlayerAttack(caster, target) && !target.isInvulnerableTo(source);
    }

    // A beam's hit, flagged as Vault Hunters flags the Prism's own (ability power, effect and area damage). It goes
    // through the immunity a mob has just after being hurt, and leaves that immunity as it was, so the beam doesn't
    // blunt the next hit from anything else either. Sparks and the hurt sound come at most every HURT_SOUND_TICKS.
    private static void hurt(Prism prism, LivingEntity target, DamageSource source, float damage, float fraction, long now) {
        if (damage <= 0.0F || !target.isAlive()) {
            return;
        }
        Long heard = prism.heard().get(target.getId());
        boolean loud = heard == null || now - heard >= HURT_SOUND_TICKS;
        if (loud) {
            prism.heard().put(target.getId(), now);
            ArcaneLasers.sparkle(prism.level(), target.getBoundingBox().getCenter(), HIT_SPARK, 5, 0.25);
        }
        int immune = target.invulnerableTime;
        beamTarget = target;
        hushed = !loud;
        hitShare = fraction;
        target.invulnerableTime = 0;
        try {
            ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> ActiveFlags.IS_EFFECT_ATTACKING.runIfNotSet(
                    () -> ActiveFlags.IS_AOE_ATTACKING.runIfNotSet(() -> target.hurt(source, damage))));
        } finally {
            beamTarget = null;
            hushed = false;
            hitShare = 1.0F;
            target.invulnerableTime = immune;
        }
    }

    static Vec3 castingHand(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        right = right.lengthSqr() < 1.0E-6 ? Vec3.ZERO : right.normalize();
        return player.getEyePosition().add(0.0, -0.3, 0.0).add(look.scale(0.5)).add(right.scale(0.3));
    }
}

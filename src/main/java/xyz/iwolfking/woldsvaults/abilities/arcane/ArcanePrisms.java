package xyz.iwolfking.woldsvaults.abilities.arcane;

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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.util.DerivedHit;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
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
 * ticks, everything a beam touched since the last hit is hurt for that share of a pulse, through the brief immunity
 * after a hit ({@link DerivedHit}) and without knockback. Vault Hunters' target cap holds for each kind of beam in each
 * hit. A mob's hurt sound plays at most every {@link #HURT_SOUND_TICKS} ticks.</li>
 * <li>Elements: the elements the cast was made under stay with its Prism. The beam's hits carry none; once every pulse
 * interval each mob gets those elements' effects for the damage the beams dealt it in that interval, as a pulse would
 * have.</li>
 * <li>Bent light: a beam that something bends ({@link BeamOptics}) stops there and carries on along the path it is
 * given. That light hurts what it touches in the same way, for the share the bender sets.</li>
 * </ul>
 * The server works out the beams a few times a tick, so a fast sweep can't skip over anything. Clients trace the same
 * paths every frame to draw them.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ArcanePrisms {
    public static final float SPIN_DEGREES_PER_TICK = 3.0F;
    private static final int BEAM_TICKS = 2;
    private static final int SUBSTEPS = 4;
    private static final int HURT_SOUND_TICKS = 10;
    private static final double SEED_SPEED = 1.8;
    private static final int SEED_MIN_TICKS = 3;
    private static final int SEED_MAX_TICKS = 16;
    private static final Vector3f HIT_SPARK = new Vector3f(1.0F, 0.95F, 0.75F);

    private static final List<Seed> SEEDS = new ArrayList<>();
    private static final List<Prism> PRISMS = new ArrayList<>();
    @Nullable
    private static Seed landing;
    @Nullable
    private static Prism beamPrism;
    @Nullable
    private static LivingEntity beamTarget;
    @Nullable
    private static DamageSource beamSource;
    private static boolean hushed;

    private static final class Seed {
        final ArcanePrismAbility ability;
        final ServerPlayer caster;
        final ServerLevel level;
        @Nullable
        final LivingEntity target;
        final Vec3 forward;
        final float damageMultiplier;
        final long lands;
        final List<ElementalType> elements;
        Vec3 at;

        Seed(ArcanePrismAbility ability, ServerPlayer caster, @Nullable LivingEntity target, Vec3 at, Vec3 forward, float damageMultiplier, long lands,
             List<ElementalType> elements) {
            this.ability = ability;
            this.caster = caster;
            this.level = caster.getLevel();
            this.target = target;
            this.at = at;
            this.forward = forward;
            this.damageMultiplier = damageMultiplier;
            this.lands = lands;
            this.elements = elements;
        }
    }

    /**
     * A placed Prism. {@code heard} holds when each mob's hurt sound last played and {@code elementDamage} the damage
     * the beams dealt each mob since the last element release, both by entity id.
     */
    private record Prism(ServerLevel level, ServerPlayer caster, Vec3 position, double heading, long created, int duration, int interval, float reach,
                         float percent, float beamWidth, int maxTargets, boolean split, BeamOptics.Lens lens, List<ElementalType> elements,
                         Map<Integer, Long> heard, Map<Integer, Float> elementDamage) {
    }

    private ArcanePrisms() {
    }

    /**
     * Called as Vault Hunters is about to place a Prism at {@code position}: launches the seed that carries it there,
     * unless this is a seed landing. Returns whether the placing should wait for the seed.
     */
    public static boolean launch(ArcanePrismAbility ability, ServerPlayer caster, Vec3 position, Vec3 forward, float damageMultiplier) {
        if (landing != null) {
            return false;
        }
        ServerLevel level = caster.getLevel();
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, new AABB(position, position).inflate(0.05),
                        entity -> entity != caster && entity.getBoundingBox().getCenter().distanceToSqr(position) < 1.0E-4)
                .stream().findFirst().orElse(null);
        Vec3 hand = castingHand(caster);
        double distance = hand.distanceTo(position);
        int flight = Mth.clamp(Mth.ceil(distance / SEED_SPEED), SEED_MIN_TICKS, SEED_MAX_TICKS);
        long now = level.getGameTime();
        SEEDS.add(new Seed(ability, caster, target, position, forward, damageMultiplier, now + flight, ElementHelper.getActiveElements()));
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster),
                new ArcanePrismSeedMessage(hand, target == null ? -1 : target.getId(), position, now, flight));
        level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.7F);
        level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.5F, 1.6F);
        return true;
    }

    /**
     * A Prism placed by a landed seed: remembers it, with the elements its cast was made under, to turn its beams and
     * hurt what they touch for as long as it lasts, and has nearby clients draw it.
     */
    public static void created(ServerPlayer caster, Vec3 position, Vec3 forward, int durationTicks, int intervalTicks, float radius,
                               float percentAbilityPower, float beamWidth, int maxTargets) {
        ServerLevel level = caster.getLevel();
        long now = level.getGameTime();
        double heading = Math.atan2(forward.z, forward.x);
        float reach = BeamOptics.bender().prismReach(caster, radius);
        boolean split = EtchingHelper.getEtchings(caster, EtchingGearAttributes.PRISM_SPLIT).stream()
                .findFirst().map(VaultGearAttributeInstance::getValue).orElse(false);
        BeamOptics.Lens lens = BeamOptics.bender().lens();
        List<ElementalType> elements = landing == null ? ElementHelper.getActiveElements() : landing.elements;
        PRISMS.add(new Prism(level, caster, position, heading, now, Math.max(1, durationTicks), Math.max(1, intervalTicks), reach, percentAbilityPower,
                beamWidth, maxTargets, split, lens, elements, new HashMap<>(), new HashMap<>()));
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster),
                new ArcanePrismMessage(position, (float) heading, now, durationTicks, reach, split, (float) lens.bentLength(), (float) lens.bentRadius()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tickSeeds();
        for (Prism prism : List.copyOf(PRISMS)) {
            if (prism.caster().isRemoved() || !prism.caster().isAlive() || prism.caster().level != prism.level()) {
                PRISMS.remove(prism);
            } else if (prism.level().getGameTime() - prism.created() >= prism.duration()) {
                releaseElements(prism);
                PRISMS.remove(prism);
            } else {
                tick(prism);
            }
        }
    }

    @SubscribeEvent
    public static void onBeamHurt(LivingHurtEvent event) {
        if (beamPrism != null && event.getEntityLiving() == beamTarget && event.getSource() == beamSource && !beamPrism.elements().isEmpty()) {
            beamPrism.elementDamage().merge(beamTarget.getId(), event.getAmount(), Float::sum);
        }
    }

    @SubscribeEvent
    public static void onSound(PlaySoundAtEntityEvent event) {
        if (beamTarget != null && hushed && event.getEntity() == beamTarget && !beamTarget.isDeadOrDying()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SEEDS.clear();
        PRISMS.clear();
        landing = null;
        beamPrism = null;
        beamTarget = null;
        beamSource = null;
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
            landing = seed;
            try {
                ((ArcanePrismAbilityInvoker) seed.ability).woldsvaults$createPrism(seed.caster, seed.at, seed.forward, seed.damageMultiplier);
            } finally {
                landing = null;
            }
        }
    }

    /**
     * Releases a Prism's elements once per pulse interval, then every {@link #BEAM_TICKS} ticks traces every beam's
     * path (bent by anything in its way) a few times a tick since the last hit, against every enemy near enough, and
     * hurts each one any of them touched, once for each kind of beam (main, split, bent light).
     */
    private static void tick(Prism prism) {
        long now = prism.level().getGameTime();
        long age = now - prism.created();
        if (age > 0 && age % prism.interval() == 0) {
            releaseElements(prism);
        }
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
        Map<LivingEntity, Float> main = new LinkedHashMap<>();
        Map<LivingEntity, Float> split = new LinkedHashMap<>();
        Map<LivingEntity, Float> bent = new LinkedHashMap<>();
        int beams = prism.split() ? 8 : 4;
        int checks = age == 0 ? 1 : BEAM_TICKS * SUBSTEPS;
        for (int step = 1; step <= checks; step++) {
            float time = age == 0 ? 0.0F : age - BEAM_TICKS + (float) step / SUBSTEPS;
            double spin = prism.heading() + Math.toRadians(SPIN_DEGREES_PER_TICK) * time;
            for (int k = 0; k < beams; k++) {
                boolean splitBeam = prism.split() && k % 2 == 1;
                double a = spin + k * Math.PI * 2.0 / beams;
                Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
                List<BeamOptics.Ray> rays = paths.trace(prism.position(), dir, prism.reach(), prism.caster());
                float share = splitBeam ? WoldConstants.ARCANE_PRISM_SPLIT_DAMAGE_MULTIPLIER : 1.0F;
                for (BeamOptics.Ray ray : rays) {
                    if (ray.inside()) {
                        continue;
                    }
                    boolean isBent = ray.bentBy() != null;
                    Map<LivingEntity, Float> touched = isBent ? bent : splitBeam ? split : main;
                    for (LivingEntity target : targets) {
                        boolean overCap = !isBent && !touched.containsKey(target) && prism.maxTargets() > 0 && touched.size() >= prism.maxTargets();
                        if (!overCap && touched.getOrDefault(target, 0.0F) < share && BeamOptics.touches(target, ray.from(), ray.to(), prism.beamWidth())) {
                            touched.put(target, share);
                        }
                    }
                }
            }
        }
        float hit = AbilityPowerHelper.getAbilityPower(prism.caster()) * prism.percent() * BEAM_TICKS / prism.interval();
        main.forEach((target, share) -> hurt(prism, target, source, hit * share, now));
        split.forEach((target, share) -> hurt(prism, target, source, hit * share, now));
        bent.forEach((target, share) -> hurt(prism, target, source, hit * share * bentShare, now));
    }

    private static boolean canHurt(ServerPlayer caster, LivingEntity target, DamageSource source) {
        return target instanceof Mob mob && mob.isAlive() && !(mob instanceof EternalEntity) && !(mob instanceof PetEntity)
                && !IPlayerAttackIgnore.isIgnoredByPlayerAttack(caster, target) && !target.isInvulnerableTo(source);
    }

    /**
     * A beam's hit, flagged as Vault Hunters flags the Prism's own (ability power, effect and area damage) and dealt
     * without knockback through {@link DerivedHit}, so it lands through the immunity left by an earlier hit and leaves
     * that immunity as it was. Sparks and the hurt sound come at most every {@link #HURT_SOUND_TICKS} ticks.
     */
    private static void hurt(Prism prism, LivingEntity target, DamageSource source, float damage, long now) {
        if (damage <= 0.0F || !target.isAlive()) {
            return;
        }
        Long heard = prism.heard().get(target.getId());
        boolean loud = heard == null || now - heard >= HURT_SOUND_TICKS;
        if (loud) {
            prism.heard().put(target.getId(), now);
            ArcaneLasers.sparkle(target, target.getBoundingBox().getCenter(), HIT_SPARK, 5, 0.25);
        }
        beamPrism = prism;
        beamTarget = target;
        beamSource = source;
        hushed = !loud;
        try {
            WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE.runWithFlag(() -> ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> ActiveFlags.IS_EFFECT_ATTACKING.runIfNotSet(
                    () -> ActiveFlags.IS_AOE_ATTACKING.runIfNotSet(() -> DerivedHit.deal(target, source, damage)))));
        } finally {
            beamPrism = null;
            beamTarget = null;
            beamSource = null;
            hushed = false;
        }
    }

    /**
     * Applies a Prism's elements to every mob its beams hurt since the last release, each for the damage the beams
     * dealt it in that time, the way a pulse's hit would have.
     */
    private static void releaseElements(Prism prism) {
        if (prism.elementDamage().isEmpty()) {
            return;
        }
        Map<Integer, Float> dealt = new HashMap<>(prism.elementDamage());
        prism.elementDamage().clear();
        dealt.forEach((id, amount) -> {
            if (prism.level().getEntity(id) instanceof LivingEntity target && target.isAlive()) {
                ElementHelper.applyElementalEffects(target, prism.caster(), amount, prism.elements(), true);
            }
        });
    }

    static Vec3 castingHand(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        right = right.lengthSqr() < 1.0E-6 ? Vec3.ZERO : right.normalize();
        return player.getEyePosition().add(0.0, -0.3, 0.0).add(look.scale(0.5)).add(right.scale(0.3));
    }
}

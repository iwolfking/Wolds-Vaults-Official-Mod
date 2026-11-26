package xyz.iwolfking.woldsvaults.modifiers.gear.simon;

import com.mojang.math.Vector3f;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.api.util.SimonSaysGame;
import xyz.iwolfking.woldsvaults.config.SimonConfig;
import xyz.iwolfking.woldsvaults.entities.simon.PrismSeedEntity;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.abilities.arcane.ArcaneLasers;
import xyz.iwolfking.woldsvaults.abilities.arcane.ArcanePrisms;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDerivedHits;
import xyz.iwolfking.woldsvaults.network.message.SimonFxMessage;
import xyz.iwolfking.woldsvaults.network.message.SimonSequenceMessage;
import xyz.iwolfking.woldsvaults.network.message.SimonSetMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Simon's Set.
 * <ul>
 * <li>Plant ({@link ModGearAttributes#SIMON_CRYSTALS}): right click shoots a prism seed that plants a crystal where
 * it lands, one planted crystal at a time. Damage the wearer deals near a crystal grows it (Seed, Shard, Cluster,
 * Geode); unfed crystals shrink. Arcane beams (Arcane, Rail and Prism) that enter a crystal bounce from crystal to
 * crystal, then on to the nearest enemy, nudged across each by where they hit ({@link CrystalOptics}). Brittle: every
 * crystal costs Resistance.</li>
 * <li>Budding ({@link ModGearAttributes#SIMON_BUDDING}): a Lucky Hit near any of the wearer's crystals makes their
 * planted crystal throw off a new seed round itself, while they have fewer crystals than the grant, so the field stays
 * gathered round the one they placed.</li>
 * <li>Resonate ({@link ModGearAttributes#SIMON_SEQUENCE}): sneak right click with enough Shard-stage crystals lights
 * them one by one, Simon style: every crystal once, biggest first, then repeats, the whole sequence one step longer
 * than the last one for each sequence got right in a row. Clicking them back in that order bursts each one (a crystal
 * shatters on its last turn) for the wearer's ability power times the grant times its detonation value, more for every
 * sequence in the streak. A wrong click shatters the rest for less and ends the streak.</li>
 * <li>Lucky extras: Lucky Hits facet Geodes ({@link ModGearAttributes#SIMON_FACETS}: more damage, slowing, then a
 * shard field), budded seeds can be Prismatic wild cards ({@link ModGearAttributes#SIMON_PRISMATIC}), a Lucky Hit
 * roll on each click fractures the crystal so it bursts twice ({@link ModGearAttributes#SIMON_FRACTURE}), and a
 * finished chain collapses into one final blast ({@link ModGearAttributes#SIMON_FINALE}).</li>
 * </ul>
 * The client sends the clicks (the crystal it picked for an aimed hit); the server checks them and does the rest.
 * Bursts, fields and the finale are dealt as {@link RiptideDerivedHits} area damage (no lucky hits from them).
 * The memory game's own rules are in {@link SimonSaysGame}; this is the set's crystals playing it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class SimonsSet {
    private static final int SYNC_INTERVAL = 10;
    private static final int SEQUENCE_LEAD_TICKS = 10;
    private static final int FIELD_PULSE_TICKS = 10;
    private static final int COLLAPSE_TICKS = 14;
    private static final float PLANTED_SEED_GRAVITY = 0.01F;
    private static final float BUDDED_SEED_GRAVITY = 0.05F;
    private static final int BUD_FLIGHT_TICKS = 12;

    private static final Map<UUID, Owner> OWNERS = new ConcurrentHashMap<>();
    private static final Map<UUID, SimonSetMessage> SYNCED = new HashMap<>();
    private static final Map<UUID, Integer> BRITTLE = new HashMap<>();
    private static final List<ShardField> FIELDS = new ArrayList<>();
    private static final List<Collapse> COLLAPSES = new ArrayList<>();
    private static final Map<UUID, SimonSaysGame.Streak> STREAKS = new HashMap<>();

    private static final class Owner {
        final List<SimonCrystalEntity> crystals = new ArrayList<>();
        final List<PrismSeedEntity> seeds = new ArrayList<>();
        long readyTick;
        @Nullable
        Resonance resonance;
    }

    /**
     * A round of the game being played with the owner's crystals, and what the set is building up from it for the
     * finale
     */
    private static final class Resonance {
        final SimonSaysGame<SimonCrystalEntity> game;
        final float multiplier;
        final List<Vec3> bursts = new ArrayList<>();
        final List<Vector3f> colors = new ArrayList<>();
        float value;
        final int prismaticGeodes;

        Resonance(SimonSaysGame<SimonCrystalEntity> game, float multiplier) {
            this.game = game;
            this.multiplier = multiplier;
            this.prismaticGeodes = (int) game.pieces().stream().filter(crystal -> crystal.isPrismatic() && crystal.getStage() == SimonCrystalEntity.GEODE).count();
        }
    }

    private static final class ShardField {
        final ServerLevel level;
        final ServerPlayer player;
        final Vec3 center;
        final float radius;
        final float damage;
        int ticksLeft;

        ShardField(ServerLevel level, ServerPlayer player, Vec3 center, float radius, float damage, int ticks) {
            this.level = level;
            this.player = player;
            this.center = center;
            this.radius = radius;
            this.damage = damage;
            this.ticksLeft = ticks;
        }
    }

    private static final class Collapse {
        final ServerLevel level;
        final ServerPlayer player;
        final Resonance chain;
        final Vec3 center;
        int age;

        Collapse(ServerLevel level, ServerPlayer player, Resonance chain, Vec3 center) {
            this.level = level;
            this.player = player;
            this.chain = chain;
            this.center = center;
        }
    }

    private SimonsSet() {
    }

    private static AttributeSnapshot snapshot(Player player) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player);
    }

    private static int intGrant(Player player, VaultGearAttribute<Integer> attribute) {
        return snapshot(player).getAttributeValue(attribute, VaultGearAttributeTypeMerger.intSum());
    }

    private static float floatGrant(Player player, VaultGearAttribute<Float> attribute) {
        return snapshot(player).getAttributeValue(attribute, VaultGearAttributeTypeMerger.floatSum());
    }

    @Nullable
    private static SimonConfig tuning() {
        return SimonConfig.installed();
    }

    /** What the bursts, shard fields and the finale scale from */
    private static float abilityPower(ServerPlayer player) {
        return Math.max(0.0F, AbilityPowerHelper.getAbilityPower(player));
    }

    /** What a hit's growth is measured against */
    private static float attackDamage(Player player) {
        return Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private static float detonationValue(SimonConfig tuning, int stage) {
        float[] values = tuning.detonationValues;
        return values == null || values.length == 0 ? stage : values[Mth.clamp(stage, 0, values.length - 1)];
    }

    /** A right click: shoots a prism seed along the player's look, if the cooldown has passed. */
    public static void shoot(ServerPlayer player) {
        SimonConfig tuning = tuning();
        if (tuning == null || intGrant(player, ModGearAttributes.SIMON_CRYSTALS) <= 0 || !player.isAlive() || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.getLevel();
        long now = level.getGameTime();
        Owner owner = OWNERS.computeIfAbsent(player.getUUID(), id -> new Owner());
        if (now < owner.readyTick || owner.resonance != null) {
            return;
        }
        owner.readyTick = now + tuning.plantCooldownTicks;
        int maxAge = (int) Math.ceil(tuning.seedRange / Math.max(0.1F, tuning.seedSpeed)) + 5;
        PrismSeedEntity seed = new PrismSeedEntity(level, player, freeColor(owner), PLANTED_SEED_GRAVITY, maxAge);
        seed.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, tuning.seedSpeed, 0.0F);
        level.addFreshEntity(seed);
        owner.seeds.add(seed);
        sound(level, player.getEyePosition(), SoundEvents.AMETHYST_CLUSTER_PLACE, 0.8F, 1.8F);
    }

    /**
     * A seed hit a block: it becomes a Seed-stage crystal on that face (a planted one replaces the oldest planted one).
     */
    public static void plant(PrismSeedEntity seed, BlockHitResult hit) {
        if (!(seed.getOwner() instanceof ServerPlayer player) || !(seed.level instanceof ServerLevel level)) {
            return;
        }
        Owner owner = OWNERS.get(player.getUUID());
        if (owner == null) {
            return;
        }
        owner.seeds.remove(seed);
        prune(owner);
        if (!seed.isNatural()) {
            int max = Math.max(1, intGrant(player, ModGearAttributes.SIMON_CRYSTALS));
            List<SimonCrystalEntity> planted = owner.crystals.stream().filter(c -> !c.isNatural() && !inSequence(owner, c)).toList();
            for (int i = 0; i <= planted.size() - max; i++) {
                owner.crystals.remove(planted.get(i));
                fizzle(planted.get(i));
            }
        }
        SimonCrystalEntity crystal = new SimonCrystalEntity(level, player, hit.getLocation(), hit.getDirection(), seed.getColor(), seed.isNatural(), seed.isPrismatic());
        level.addFreshEntity(crystal);
        owner.crystals.add(crystal);
        sound(level, crystal.center(), SoundEvents.AMETHYST_CLUSTER_PLACE, 1.0F, 1.4F);
        fx(level, SimonFxMessage.Kind.EMERGE, crystal.position(), crystal.getFace(), crystal.getStage(), crystal.shownColor(0.0F), 0.0F);
    }

    public static void seedExpired(PrismSeedEntity seed) {
        if (seed.getOwner() instanceof ServerPlayer player) {
            Owner owner = OWNERS.get(player.getUUID());
            if (owner != null) {
                owner.seeds.remove(seed);
            }
        }
        if (seed.level instanceof ServerLevel level) {
            level.sendParticles(amethystShards(), seed.getX(), seed.getY(), seed.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
        }
    }

    /** The first Simon colour none of the owner's crystals or seeds has (they repeat past four) */
    private static SimonCrystalEntity.Color freeColor(Owner owner) {
        SimonCrystalEntity.Color[] colors = SimonCrystalEntity.Color.values();
        Set<SimonCrystalEntity.Color> used = new HashSet<>();
        owner.crystals.forEach(crystal -> used.add(crystal.getColor()));
        owner.seeds.forEach(seed -> used.add(seed.getColor()));
        for (SimonCrystalEntity.Color color : colors) {
            if (!used.contains(color)) {
                return color;
            }
        }
        return colors[(owner.crystals.size() + owner.seeds.size()) % colors.length];
    }

    /** Damage the owner deals to an enemy feeds every crystal of theirs within reach of it, once a tick each. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !(event.getEntityLiving() instanceof Mob mob) || !GearSetHelper.isEnemy(mob)) {
            return;
        }
        Owner owner = OWNERS.get(player.getUUID());
        SimonConfig tuning = tuning();
        if (owner == null || tuning == null || owner.resonance != null) {
            return;
        }
        float points = Mth.clamp(event.getAmount() / attackDamage(player), 0.0F, tuning.maxGrowthPerHit * ArcanePrisms.hitShare());
        if (points <= 0.0F) {
            return;
        }
        Vec3 at = mob.getBoundingBox().getCenter();
        long now = player.getLevel().getGameTime();
        prune(owner);
        for (SimonCrystalEntity crystal : owner.crystals) {
            if (crystal.center().distanceToSqr(at) > tuning.growRadius * tuning.growRadius || crystal.lastFedTick == now) {
                continue;
            }
            crystal.lastFedTick = now;
            crystal.unfedTicks = 0;
            if (crystal.getStage() >= SimonCrystalEntity.GEODE) {
                continue;
            }
            crystal.growth += points;
            if (crystal.growth >= tuning.growthPerStage) {
                crystal.growth = 0.0F;
                crystal.setStage(crystal.getStage() + 1);
                Vec3 c = crystal.center();
                sound(crystal.level, c, crystal.getStage() == SimonCrystalEntity.GEODE ? SoundEvents.AMETHYST_CLUSTER_PLACE : SoundEvents.AMETHYST_BLOCK_CHIME,
                        1.0F, 0.7F + crystal.getStage() * 0.25F);
                sound(crystal.level, c, SoundEvents.ROOTED_DIRT_BREAK, 0.8F, 0.8F);
                fx(player.getLevel(), SimonFxMessage.Kind.EMERGE, crystal.position(), crystal.getFace(), crystal.getStage(), crystal.shownColor(0.0F), 0.0F);
            }
        }
    }

    /**
     * A Lucky Hit by a wearer's own swing (not ability damage, projectiles or anything Vault Hunters lets roll under
     * its ability-power flag) near any of the wearer's crystals: their planted crystal buds, and the nearest Geode
     * short of facets gains one.
     */
    public static void onLuckyHit(ServerPlayer player, LivingHurtEvent event) {
        Owner owner = OWNERS.get(player.getUUID());
        SimonConfig tuning = tuning();
        if (owner == null || tuning == null || owner.resonance != null
                || event.getSource().getDirectEntity() != player || ActiveFlags.IS_AP_ATTACKING.isSet()) {
            return;
        }
        Vec3 at = event.getEntityLiving().getBoundingBox().getCenter();
        prune(owner);
        int cap = intGrant(player, ModGearAttributes.SIMON_BUDDING);
        if (cap > 0 && crystalCount(owner) < cap && nearest(owner, at, tuning.growRadius, crystal -> true) != null) {
            SimonCrystalEntity planted = owner.crystals.stream().filter(crystal -> !crystal.isNatural() && !crystal.isRemoved()).findFirst().orElse(null);
            if (planted != null) {
                bud(player, owner, planted, tuning);
            }
        }
        int facets = Math.min(3, intGrant(player, ModGearAttributes.SIMON_FACETS));
        if (facets > 0) {
            SimonCrystalEntity geode = nearest(owner, at, tuning.growRadius,
                    crystal -> crystal.getStage() == SimonCrystalEntity.GEODE && crystal.getFacets() < facets);
            if (geode != null) {
                geode.setFacets(geode.getFacets() + 1);
                geode.unfedTicks = 0;
                Vec3 c = geode.center();
                sound(geode.level, c, SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 1.6F + geode.getFacets() * 0.15F);
                sound(geode.level, c, SoundEvents.AMETHYST_CLUSTER_PLACE, 1.0F, 1.4F);
                fx(player.getLevel(), SimonFxMessage.Kind.FACET, c, Direction.UP, geode.getFacets(), geode.shownColor(0.0F),
                        (float) geode.getBoundingBox().getXsize());
            }
        }
    }

    /** Every crystal the owner has, planted or budded, and every seed on its way */
    private static int crystalCount(Owner owner) {
        return owner.crystals.size() + owner.seeds.size();
    }

    /** Throws a new seed off the planted crystal on to a nearby spot of ground it can see */
    private static void bud(ServerPlayer player, Owner owner, SimonCrystalEntity parent, SimonConfig tuning) {
        ServerLevel level = player.getLevel();
        Vec3 from = parent.center();
        Vec3 land = landingSpot(level, owner, parent, tuning);
        if (land == null) {
            return;
        }
        boolean prismatic = level.getRandom().nextFloat() < floatGrant(player, ModGearAttributes.SIMON_PRISMATIC);
        PrismSeedEntity seed = new PrismSeedEntity(level, player, from, freeColor(owner), prismatic, BUDDED_SEED_GRAVITY, BUD_FLIGHT_TICKS * 4);
        seed.target = land;
        Vec3 d = land.subtract(from);
        seed.setDeltaMovement(d.x / BUD_FLIGHT_TICKS, d.y / BUD_FLIGHT_TICKS + BUDDED_SEED_GRAVITY * (BUD_FLIGHT_TICKS - 1) / 2.0, d.z / BUD_FLIGHT_TICKS);
        level.addFreshEntity(seed);
        owner.seeds.add(seed);
        sound(level, from, SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, prismatic ? 2.0F : 1.3F);
        ArcaneLasers.sparkle(parent, from, parent.shownColor(0.0F), 10, 0.3);
    }

    /**
     * A spot of open ground the crystal can see, at least budSpacing from every crystal and every seed already on its
     * way
     */
    @Nullable
    private static Vec3 landingSpot(ServerLevel level, Owner owner, SimonCrystalEntity parent, SimonConfig tuning) {
        Vec3 base = parent.position();
        Vec3 from = parent.center();
        double near = Math.max(1.5, tuning.budSpacing);
        double far = Math.max(tuning.budRange, near + 1.0);
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = near + level.getRandom().nextDouble() * (far - near);
            double x = base.x + Math.cos(angle) * distance;
            double z = base.z + Math.sin(angle) * distance;
            BlockHitResult ground = level.clip(new ClipContext(new Vec3(x, base.y + 3.0, z), new Vec3(x, base.y - 5.0, z),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, parent));
            if (ground.getType() != HitResult.Type.BLOCK || ground.getDirection() != Direction.UP) {
                continue;
            }
            Vec3 spot = ground.getLocation();
            if (farFromOthers(owner, spot, tuning.budSpacing) && CrystalOptics.canSee(parent.level, from, spot.add(0.0, 0.3, 0.0), parent)) {
                return spot;
            }
        }
        return null;
    }

    private static boolean farFromOthers(Owner owner, Vec3 spot, float spacing) {
        double min = spacing * spacing;
        for (SimonCrystalEntity crystal : owner.crystals) {
            if (!crystal.isRemoved() && crystal.position().distanceToSqr(spot) < min) {
                return false;
            }
        }
        for (PrismSeedEntity seed : owner.seeds) {
            if (!seed.isRemoved() && seed.target != null && seed.target.distanceToSqr(spot) < min) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sneak right click: with enough Shard-stage crystals they light up one after another, then wait to be clicked in
     * that order. Every crystal comes up once, biggest first; after that the sequence goes on with repeats until it is
     * one step longer than the last sequence, for as long as the player keeps getting them right.
     */
    public static void resonate(ServerPlayer player) {
        SimonConfig tuning = tuning();
        Owner owner = OWNERS.get(player.getUUID());
        if (tuning == null || floatGrant(player, ModGearAttributes.SIMON_SEQUENCE) <= 0.0F) {
            return;
        }
        if (owner == null || owner.resonance != null) {
            fizz(player);
            return;
        }
        prune(owner);
        List<SimonCrystalEntity> members = new ArrayList<>(owner.crystals.stream().filter(crystal -> crystal.getStage() >= SimonCrystalEntity.SHARD).toList());
        if (members.size() < Math.max(Math.max(1, tuning.resonateMinCrystals), intGrant(player, ModGearAttributes.SIMON_BUDDING))) {
            fizz(player);
            return;
        }
        members.sort(Comparator.comparingInt(SimonCrystalEntity::getStage).reversed().thenComparingDouble(crystal -> crystal.distanceToSqr(player)));
        SimonSaysGame.Streak streak = STREAKS.computeIfAbsent(player.getUUID(), id -> new SimonSaysGame.Streak());
        SimonSaysGame.Timing timing = new SimonSaysGame.Timing(SEQUENCE_LEAD_TICKS, tuning.showTicks, tuning.showGapTicks, tuning.inputBaseTicks, tuning.inputTicksPerCrystal);
        SimonSaysGame<SimonCrystalEntity> game = SimonSaysGame.start(members, streak.lengthFor(members.size(), tuning.maxSequenceLength), timing, SimonCrystalEntity::isPrismatic, player.getRandom());
        float multiplier = lengthMultiplier(tuning, game.length());
        owner.resonance = new Resonance(game, multiplier);
        for (SimonCrystalEntity crystal : members) {
            crystal.setLight(SimonCrystalEntity.ARMED);
        }
        sound(player, SoundEvents.BEACON_ACTIVATE, 1.0F, 1.6F);
        player.displayClientMessage(new TextComponent("Resonating: " + game.length() + " to remember"
                + (multiplier > 1.0F ? " (+" + Math.round((multiplier - 1.0F) * 100) + "% damage)" : "")), true);
    }

    /** The damage multiplier for every explosion in a sequence of this length: lengthDamage per step past resonateMinCrystals. */
    private static float lengthMultiplier(SimonConfig tuning, int length) {
        return 1.0F + tuning.lengthDamage * Math.max(0, length - tuning.resonateMinCrystals);
    }

    private static boolean inSequence(Owner owner, SimonCrystalEntity crystal) {
        return owner.resonance != null && owner.resonance.game.includes(crystal);
    }

    /** Lights the steps one at a time with rising chimes, then waits for the clicks */
    private static void tickSequence(ServerPlayer player, Owner owner, SimonConfig tuning) {
        Resonance resonance = owner.resonance;
        if (resonance == null) {
            return;
        }
        SimonSaysGame<SimonCrystalEntity> game = resonance.game;
        if (!game.isPlayerTurn() && game.pieces().stream().anyMatch(Entity::isRemoved)) {
            endSequence(player, owner);
            return;
        }
        SimonSaysGame.Cue<SimonCrystalEntity> cue = game.tick();
        switch (cue.kind()) {
            case LIGHT -> {
                cue.piece().setLight(SimonCrystalEntity.LIT);
                chime(cue.piece());
            }
            case DIM -> cue.piece().setLight(SimonCrystalEntity.ARMED);
            case PLAYER_TURN -> ModNetwork.sendToClient(new SimonSequenceMessage(game.pieces().stream().mapToInt(Entity::getId).toArray()), player);
            case TIMED_OUT -> fail(player, owner, resonance, tuning);
        }
    }

    /**
     * The crystal's own note, the same each time it lights up or is clicked, so the sequence can be followed by ear:
     * red C, blue D, green E, yellow G. A Prismatic crystal rings a C major chord (C, E, G) instead. A faint amethyst
     * shimmer sits under the note.
     */
    private static void chime(SimonCrystalEntity crystal) {
        Vec3 c = crystal.center();
        if (crystal.isPrismatic()) {
            for (SimonCrystalEntity.Color note : new SimonCrystalEntity.Color[]{SimonCrystalEntity.Color.RED, SimonCrystalEntity.Color.GREEN, SimonCrystalEntity.Color.YELLOW}) {
                sound(crystal.level, c, SoundEvents.NOTE_BLOCK_CHIME, 1.0F, note.pitch());
            }
        } else {
            sound(crystal.level, c, SoundEvents.NOTE_BLOCK_CHIME, 1.5F, crystal.getColor().pitch());
        }
        sound(crystal.level, c, SoundEvents.AMETHYST_BLOCK_CHIME, 0.3F, 1.2F);
    }

    /** An aimed left click from the client: checks the player can reach and see the crystal, then hits it. */
    public static void clickCrystal(ServerPlayer player, int crystalId) {
        SimonConfig tuning = tuning();
        if (tuning == null || !(player.getLevel().getEntity(crystalId) instanceof SimonCrystalEntity crystal) || crystal.isRemoved()) {
            return;
        }
        float range = tuning.clickRange + 2.0F;
        Vec3 eye = player.getEyePosition();
        AABB box = crystal.getBoundingBox().inflate(0.5);
        Vec3 c = crystal.center();
        double reach = box.contains(eye) ? 0.0 : box.clip(eye, c).map(hit -> hit.distanceTo(eye)).orElse(eye.distanceTo(c));
        if (reach > range) {
            return;
        }
        BlockHitResult wall = player.level.clip(new ClipContext(eye, c, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (wall.getType() == HitResult.Type.BLOCK && wall.getLocation().distanceTo(eye) < reach - 0.5) {
            return;
        }
        onCrystalHit(player, crystal);
    }

    /**
     * A click on one of the owner's crystals: the next step bursts. Once a sequence, a Prismatic crystal clicked out of
     * turn stands in for the next step, which goes off as if it had been clicked; any other crystal fails the sequence.
     */
    public static void onCrystalHit(ServerPlayer player, SimonCrystalEntity crystal) {
        Owner owner = OWNERS.get(player.getUUID());
        SimonConfig tuning = tuning();
        if (owner == null || tuning == null || !player.getUUID().equals(crystal.getOwner())) {
            return;
        }
        Resonance resonance = owner.resonance;
        Vec3 c = crystal.center();
        SimonSaysGame.Press<SimonCrystalEntity> press = resonance == null ? null : resonance.game.press(crystal);
        if (press == null || press.outcome() == SimonSaysGame.Outcome.IGNORED) {
            sound(crystal.level, c, SoundEvents.AMETHYST_BLOCK_HIT, 0.8F, 1.2F);
            return;
        }
        if (press.outcome() == SimonSaysGame.Outcome.WRONG) {
            fail(player, owner, resonance, tuning);
            return;
        }
        SimonCrystalEntity step = press.step();
        if (press.outcome() == SimonSaysGame.Outcome.STAND_IN) {
            sound(crystal.level, c, SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 2.0F);
            ArcaneLasers.beam(player, List.of(c, step.center()), 0xFFFFFF, 0.3F, 10);
        }
        release(player, owner, resonance, step, tuning, press.lastTurn());
        if (press.finished()) {
            complete(player, owner, resonance, tuning);
        }
    }

    /** A correct click: the crystal bursts (twice, if a Lucky Hit roll fractures it), shattering on its last turn */
    private static void release(ServerPlayer player, Owner owner, Resonance resonance, SimonCrystalEntity crystal, SimonConfig tuning, boolean last) {
        boolean fractured = snapshot(player).getAttributeValue(ModGearAttributes.SIMON_FRACTURE, VaultGearAttributeTypeMerger.anyTrue())
                && player.getRandom().nextFloat() < LuckyHitHelper.getLuckyHitChance(player);
        Vec3 c = crystal.center();
        resonance.bursts.add(c);
        resonance.colors.add(crystal.shownColor(0.0F));
        resonance.value += detonationValue(tuning, crystal.getStage());
        chime(crystal);
        if (fractured) {
            sound(crystal.level, c, SoundEvents.GLASS_BREAK, 1.2F, 1.4F);
            player.getLevel().sendParticles(ParticleTypes.CRIT, c.x, c.y, c.z, 20, 0.4, 0.5, 0.4, 0.3);
        }
        burst(player, owner, crystal, tuning, resonance.multiplier, fractured ? 2 : 1, last);
    }

    /**
     * The whole sequence right: the streak grows (the next one is a step longer) and the bursts collapse into the
     * finale
     */
    private static void complete(ServerPlayer player, Owner owner, Resonance resonance, SimonConfig tuning) {
        SimonSaysGame.Streak streak = STREAKS.computeIfAbsent(player.getUUID(), id -> new SimonSaysGame.Streak());
        streak.won(resonance.game.length());
        if (floatGrant(player, ModGearAttributes.SIMON_FINALE) > 0.0F && !resonance.bursts.isEmpty()) {
            Vec3 center = Vec3.ZERO;
            for (Vec3 at : resonance.bursts) {
                center = center.add(at);
            }
            center = center.scale(1.0 / resonance.bursts.size());
            COLLAPSES.add(new Collapse(player.getLevel(), player, resonance, center));
        }
        sound(player, SoundEvents.PLAYER_LEVELUP, 0.8F, 1.4F);
        int next = Math.min(streak.lastLength() + 1, Math.max(tuning.maxSequenceLength, streak.lastLength()));
        player.displayClientMessage(new TextComponent("Sequence complete! Next is " + next + " long, +"
                + Math.round((lengthMultiplier(tuning, next) - 1.0F) * 100) + "% damage"), true);
        endSequence(player, owner);
    }

    /**
     * A wrong click or too slow: a buzz, the streak is gone, and every crystal left in the chain shatters for part of
     * its burst
     */
    private static void fail(ServerPlayer player, Owner owner, Resonance resonance, SimonConfig tuning) {
        sound(player, SoundEvents.NOTE_BLOCK_DIDGERIDOO, 1.2F, 0.5F);
        sound(player, SoundEvents.NOTE_BLOCK_BASS, 1.2F, 0.5F);
        SimonSaysGame.Streak streak = STREAKS.get(player.getUUID());
        if (streak != null && streak.wins() > 0) {
            player.displayClientMessage(new TextComponent("Wrong crystal: streak of " + streak.wins() + " lost"), true);
        }
        STREAKS.remove(player.getUUID());
        for (SimonCrystalEntity crystal : new ArrayList<>(resonance.game.pieces())) {
            if (!crystal.isRemoved()) {
                burst(player, owner, crystal, tuning, tuning.failDamage * resonance.multiplier, 1, true);
            }
        }
        endSequence(player, owner);
    }

    private static void endSequence(ServerPlayer player, Owner owner) {
        for (SimonCrystalEntity crystal : owner.resonance.game.pieces()) {
            if (!crystal.isRemoved()) {
                crystal.setLight(SimonCrystalEntity.DARK);
            }
        }
        owner.resonance = null;
        ModNetwork.sendToClient(new SimonSequenceMessage(new int[0]), player);
    }

    /**
     * A crystal goes off: every enemy in reach takes its burst (more with a facet, slowed with two, a shard field with
     * three). On its last turn it shatters into flying amethyst; on an earlier one it only pulses.
     */
    private static void burst(ServerPlayer player, Owner owner, SimonCrystalEntity crystal, SimonConfig tuning, float multiplier, int times, boolean shatter) {
        ServerLevel level = player.getLevel();
        float value = detonationValue(tuning, crystal.getStage());
        int facets = crystal.getFacets();
        float damage = abilityPower(player) * floatGrant(player, ModGearAttributes.SIMON_SEQUENCE) * value * multiplier * (facets >= 1 ? 1.0F + tuning.facetDamage : 1.0F);
        float radius = tuning.burstRadius + tuning.burstRadiusPerValue * value;
        Vec3 c = crystal.center();
        if (damage > 0.0F) {
            for (Mob mob : GearSetHelper.enemiesNear(level, c, radius)) {
                for (int i = 0; i < times; i++) {
                    RiptideDerivedHits.queue(player, mob, damage);
                }
                if (facets >= 2) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, tuning.facetSlowTicks, 2));
                }
            }
            if (facets >= 3) {
                FIELDS.add(new ShardField(level, player, c, tuning.shardFieldRadius, damage * tuning.shardFieldDamage, tuning.shardFieldTicks));
            }
        }
        Vector3f color = crystal.shownColor(0.0F);
        fx(level, shatter ? SimonFxMessage.Kind.SHATTER : SimonFxMessage.Kind.PULSE, c, crystal.getFace(), crystal.getStage(), color, radius);
        if (shatter) {
            sound(level, c, SoundEvents.AMETHYST_BLOCK_BREAK, 1.5F, 0.8F);
            sound(level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.5F, 1.1F);
            sound(level, c, SoundEvents.GENERIC_EXPLODE, 0.6F, 1.5F);
            owner.crystals.remove(crystal);
            owner.resonance.game.remove(crystal);
            crystal.discard();
        } else {
            sound(level, c, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 0.9F);
        }
    }

    private static void fizz(ServerPlayer player) {
        sound(player, SoundEvents.AMETHYST_BLOCK_HIT, 0.8F, 0.6F);
    }

    /** A crystal's server tick: gone with its owner or when left behind, shrinking when unfed. */
    public static void tickCrystal(SimonCrystalEntity crystal) {
        SimonConfig tuning = tuning();
        UUID ownerId = crystal.getOwner();
        Owner owner = ownerId == null ? null : OWNERS.get(ownerId);
        ServerPlayer player = ownerId == null || crystal.getServer() == null ? null : crystal.getServer().getPlayerList().getPlayer(ownerId);
        if (tuning == null || owner == null || player == null || !owner.crystals.contains(crystal)) {
            crystal.discard();
            return;
        }
        boolean locked = inSequence(owner, crystal);
        if (!player.isAlive() || player.level != crystal.level || player.distanceToSqr(crystal) > tuning.leashRange * tuning.leashRange) {
            owner.crystals.remove(crystal);
            if (locked) {
                endSequence(player, owner);
            }
            fizzle(crystal);
            return;
        }
        if (!locked) {
            crystal.unfedTicks++;
            if (crystal.unfedTicks >= tuning.shrinkTicks) {
                crystal.unfedTicks = 0;
                crystal.growth = 0.0F;
                if (crystal.getStage() > SimonCrystalEntity.SEED) {
                    crystal.setStage(crystal.getStage() - 1);
                    sound(crystal.level, crystal.center(), SoundEvents.AMETHYST_BLOCK_STEP, 1.0F, 0.6F);
                } else if (crystal.isNatural()) {
                    owner.crystals.remove(crystal);
                    fizzle(crystal);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();
        Owner owner = OWNERS.get(id);
        SimonConfig tuning = tuning();
        if (tuning == null) {
            return;
        }
        if (player.tickCount % SYNC_INTERVAL == 0) {
            boolean canShoot = intGrant(player, ModGearAttributes.SIMON_CRYSTALS) > 0;
            boolean canResonate = floatGrant(player, ModGearAttributes.SIMON_SEQUENCE) > 0.0F;
            SimonSetMessage state = new SimonSetMessage(canShoot, tuning.plantCooldownTicks, canResonate, tuning.clickRange);
            if (!state.equals(SYNCED.getOrDefault(id, new SimonSetMessage(false, tuning.plantCooldownTicks, false, tuning.clickRange)))) {
                SYNCED.put(id, state);
                ModNetwork.sendToClient(state, player);
            }
            if (owner != null && !canShoot) {
                clear(id);
                owner = null;
            }
        }
        if (owner != null) {
            prune(owner, player.level);
            tickSequence(player, owner, tuning);
            if (owner.crystals.isEmpty() && owner.seeds.isEmpty() && owner.resonance == null && player.getLevel().getGameTime() >= owner.readyTick) {
                OWNERS.remove(id);
                owner = null;
            }
        }
        int count = owner == null ? 0 : owner.crystals.size();
        if (count != BRITTLE.getOrDefault(id, 0)) {
            BRITTLE.put(id, count);
            AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(player);
        }
    }

    /** Brittle, added while the player's attribute snapshot is built: each crystal they have costs some Resistance. */
    public static void addBrittle(ServerPlayer player, AttributeSnapshot snapshot) {
        SimonConfig tuning = tuning();
        Owner owner = OWNERS.get(player.getUUID());
        if (tuning == null || owner == null) {
            return;
        }
        long count = owner.crystals.stream().filter(crystal -> !crystal.isRemoved()).count();
        if (count > 0) {
            snapshot.addAttributeValue(iskallia.vault.init.ModGearAttributes.RESISTANCE, -tuning.brittleResistance * count);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        FIELDS.removeIf(field -> field.player.isRemoved() || field.player.level != field.level || --field.ticksLeft < 0);
        FIELDS.forEach(SimonsSet::tickField);
        SimonConfig tuning = tuning();
        COLLAPSES.removeIf(collapse -> collapse.player.isRemoved() || collapse.player.level != collapse.level || tuning == null || tickCollapse(collapse, tuning));
    }

    /** A lingering patch of shards that cuts every enemy standing in it */
    private static void tickField(ShardField field) {
        BlockParticleOption shards = amethystShards();
        for (int i = 0; i < 3; i++) {
            double angle = field.level.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(field.level.getRandom().nextDouble()) * field.radius;
            field.level.sendParticles(shards, field.center.x + Math.cos(angle) * distance, field.center.y - 0.3, field.center.z + Math.sin(angle) * distance, 1, 0.0, 0.2, 0.0, 0.0);
        }
        if (field.ticksLeft % FIELD_PULSE_TICKS == 0) {
            for (Mob mob : GearSetHelper.enemiesNear(field.level, field.center, field.radius)) {
                RiptideDerivedHits.queue(field.player, mob, field.damage);
            }
            sound(field.level, field.center, SoundEvents.AMETHYST_BLOCK_STEP, 0.8F, 1.5F);
        }
    }

    /** Every burst streams to the middle of the shape, then one blast sized by the chain's total value */
    private static boolean tickCollapse(Collapse collapse, SimonConfig tuning) {
        Resonance chain = collapse.chain;
        Vec3 c = collapse.center;
        collapse.age++;
        double progress = Math.min(1.0, collapse.age / (double) COLLAPSE_TICKS);
        for (int i = 0; i < chain.bursts.size(); i++) {
            Vec3 at = chain.bursts.get(i).lerp(c, progress * progress);
            ArcaneLasers.sparkle(collapse.player, at, chain.colors.get(i), 4, 0.12);
            collapse.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 2, 0.1, 0.1, 0.1, 0.05);
        }
        if (collapse.age == COLLAPSE_TICKS - 4) {
            sound(collapse.level, c, SoundEvents.BEACON_POWER_SELECT, 1.5F, 0.6F);
        }
        if (collapse.age < COLLAPSE_TICKS) {
            return false;
        }
        ServerPlayer player = collapse.player;
        float doubling = (float) Math.min(tuning.finaleMaxPrismaticMultiplier, Math.pow(2.0, chain.prismaticGeodes));
        float radius = Math.min(tuning.finaleMaxRadius, tuning.finaleRadius + tuning.finaleRadiusPerValue * chain.value);
        float damage = abilityPower(player) * floatGrant(player, ModGearAttributes.SIMON_FINALE) * chain.value * chain.multiplier * doubling;
        for (Mob mob : GearSetHelper.enemiesNear(collapse.level, c, radius)) {
            RiptideDerivedHits.queue(player, mob, damage);
        }
        Vector3f blend = new Vector3f(0.0F, 0.0F, 0.0F);
        for (Vector3f color : chain.colors) {
            blend.add(color);
        }
        float brightest = Math.max(blend.x(), Math.max(blend.y(), blend.z()));
        if (brightest > 0.0F) {
            blend.mul(1.0F / brightest);
        } else {
            blend.set(1.0F, 1.0F, 1.0F);
        }
        ModNetwork.sendNear(collapse.level, c, 64.0 + radius * 3.0,
                new SimonFxMessage(SimonFxMessage.Kind.FINALE, c.x, c.y, c.z, Direction.UP, chain.prismaticGeodes, ColorUtil.rgb(blend), radius));
        if (chain.prismaticGeodes > 0) {
            sound(collapse.level, c, SoundEvents.END_PORTAL_SPAWN, 1.0F, 1.6F);
        }
        sound(collapse.level, c, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F);
        sound(collapse.level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 2.0F, 0.6F);
        sound(collapse.level, c, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.2F, 1.3F);
        sound(collapse.level, c, SoundEvents.TOTEM_USE, 0.6F, 1.3F);
        return true;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        forget(event.getPlayer().getUUID());
    }

    /** Entering or leaving a vault drops the player's crystals, seeds, fields and streak. */
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        forget(event.getPlayer().getUUID());
    }

    private static void forget(UUID id) {
        clear(id);
        SYNCED.remove(id);
        BRITTLE.remove(id);
        STREAKS.remove(id);
        FIELDS.removeIf(field -> field.player.getUUID().equals(id));
        COLLAPSES.removeIf(collapse -> collapse.player.getUUID().equals(id));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        OWNERS.clear();
        SYNCED.clear();
        BRITTLE.clear();
        STREAKS.clear();
        FIELDS.clear();
        COLLAPSES.clear();
    }

    private static void clear(UUID playerId) {
        Owner owner = OWNERS.remove(playerId);
        if (owner != null) {
            owner.crystals.forEach(SimonsSet::fizzle);
            owner.seeds.forEach(Entity::discard);
        }
    }

    private static void prune(Owner owner) {
        owner.crystals.removeIf(Entity::isRemoved);
        owner.seeds.removeIf(Entity::isRemoved);
    }

    private static void prune(Owner owner, Level level) {
        owner.crystals.removeIf(crystal -> crystal.isRemoved() || crystal.level != level);
        owner.seeds.removeIf(seed -> seed.isRemoved() || seed.level != level);
    }

    private static void fizzle(SimonCrystalEntity crystal) {
        if (crystal.level instanceof ServerLevel level && !crystal.isRemoved()) {
            Vec3 c = crystal.center();
            level.sendParticles(amethystShards(), c.x, c.y, c.z, 10, 0.2, 0.2, 0.2, 0.1);
            sound(level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 0.8F, 1.2F);
        }
        crystal.discard();
    }

    @Nullable
    private static SimonCrystalEntity nearest(Owner owner, Vec3 at, float range, Predicate<SimonCrystalEntity> filter) {
        SimonCrystalEntity best = null;
        double bestDistance = range * range;
        for (SimonCrystalEntity crystal : owner.crystals) {
            double distance = crystal.center().distanceToSqr(at);
            if (!crystal.isRemoved() && distance <= bestDistance && filter.test(crystal)) {
                best = crystal;
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Whether anyone has crystals out (so beams need checking against them at all). */
    static boolean hasCrystals() {
        return !OWNERS.isEmpty();
    }

    /** How far a Prism's beams reach: further for a Simon's Set wearer. */
    static float prismReach(ServerPlayer player, float radius) {
        SimonConfig tuning = tuning();
        if (tuning == null || intGrant(player, ModGearAttributes.SIMON_CRYSTALS) <= 0) {
            return radius;
        }
        return radius * Math.max(1.0F, tuning.prismReach);
    }

    /**
     * A beam from start to end. If it enters a crystal, a copy of it bounces from crystal to crystal, then on to the
     * nearest enemy, each bounce nudged across its target by where the beam hit (see CrystalOptics). Every enemy along
     * the bounces is hurt once, for the player's ability power times the beam's share times arcaneReflectDamage. The
     * bounces' hits are area damage (Vault Hunters' area flag on top of its ability-power one), so they roll no Lucky
     * Hits and set off no on-hit procs. The original beam carries on as normal.
     */
    static void refract(ServerPlayer player, Vec3 start, Vec3 end, float percentAbilityPower, int beamTicks, float beamWidth) {
        SimonConfig tuning = tuning();
        double length = start.distanceTo(end);
        if (!hasCrystals() || tuning == null || length < 1.0E-3) {
            return;
        }
        ServerLevel level = player.getLevel();
        List<SimonCrystalEntity> crystals = CrystalOptics.crystalsNear(level, start.lerp(end, 0.5), length / 2.0 + tuning.reflectRange + 2.0);
        if (crystals.isEmpty()) {
            return;
        }
        Vec3 dir = end.subtract(start).scale(1.0 / length);
        BeamOptics.Lens lens = new BeamOptics.Lens(tuning.reflectRange, tuning.reflectRadius);
        List<BeamOptics.Ray> rays = CrystalOptics.trace(level, crystals, start, dir, length, lens, player);
        if (rays.size() < 2) {
            return;
        }
        float damage = AbilityPowerHelper.getAbilityPower(player) * percentAbilityPower * tuning.arcaneReflectDamage;
        DamageSource source = DamageSource.playerAttack(player);
        Set<Integer> struck = new HashSet<>();
        for (int i = 1; i < rays.size(); i++) {
            BeamOptics.Ray ray = rays.get(i);
            if (ray.inside()) {
                ArcaneLasers.beam(player, List.of(ray.from(), ray.to()), 0xFFFFFF, beamWidth * 0.7F, beamTicks);
                continue;
            }
            for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(ray.from(), ray.to()).inflate(1.5), GearSetHelper::isEnemy)) {
                if (damage > 0.0F && BeamOptics.touches(mob, ray.from(), ray.to(), 0.6) && struck.add(mob.getId())) {
                    ActiveFlags.IS_AOE_ATTACKING.push();
                    try {
                        ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> mob.hurt(source, damage));
                    } finally {
                        ActiveFlags.IS_AOE_ATTACKING.pop();
                    }
                }
            }
            ArcaneLasers.beam(player, List.of(ray.from(), ray.to()), ColorUtil.rgb(ray.bentBy().shownColor(0.0F)), beamWidth, beamTicks);
        }
    }

    private static void sound(Level level, Vec3 at, SoundEvent event, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, event, SoundSource.PLAYERS, volume, pitch);
    }

    private static void sound(Entity at, SoundEvent event, float volume, float pitch) {
        sound(at.level, at.position(), event, volume, pitch);
    }

    private static BlockParticleOption amethystShards() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState());
    }

    /** A client effect for everyone near the spot */
    private static void fx(ServerLevel level, SimonFxMessage.Kind kind, Vec3 at, Direction face, int stage, Vector3f color, float size) {
        ModNetwork.sendNear(level, at, 64.0, new SimonFxMessage(kind, at.x, at.y, at.z, face, stage, ColorUtil.rgb(color), size));
    }
}

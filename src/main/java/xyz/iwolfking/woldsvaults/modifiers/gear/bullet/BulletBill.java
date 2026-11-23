package xyz.iwolfking.woldsvaults.modifiers.gear.bullet;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.skill.ability.effect.DashDamageAbility;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.BulletBillConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.accessors.PlayerJumpAccessor;
import xyz.iwolfking.woldsvaults.mixins.accessors.ServerGamePacketListenerImplAccessor;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDerivedHits;
import xyz.iwolfking.woldsvaults.modifiers.gear.simon.CrystalOptics;
import xyz.iwolfking.woldsvaults.network.message.BulletBillMessage;
import xyz.iwolfking.woldsvaults.network.message.SimonFxMessage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Bullet Bill set: a helmet and leggings. Double tapping jump turns the wearer into a Bullet Bill for as long as
 * their bullet_bill grant says, and the next one waits for the cooldown after the form ends.
 * The flight is PneumaticCraft: Repressurized's own jet boots, run through {@link BillJets}, with jump as the throttle.
 * The jets always run and carry the Bill wherever its rider looks, faster the longer it dives and slower the longer it
 * climbs: at a cruise with jump let go, at the boots' full thrust with it held, and faster still in a Bullet Dash. With
 * cruiseSpeed 0 they only run while jump is held and the Bill hovers otherwise, as the boots do. Both sides run them each tick (the client moves its own player; the server keeps the same motion for its
 * movement checks and the wall hits). No air or pressure is spent: the form's timer is the limit instead, and no fall
 * damage is taken while it lasts or on the landing after.
 * With the bullet_bill_dash grant, Bullet Dash cast as a Bill fires the jets for a moment whether or not jump is held,
 * faster than usual, with a spin and a red pulse, and adds the grant to the dash's damage. With the
 * bullet_bill_detonation grant, right click arms the Bill: the next time it crashes into a block at speed it explodes,
 * ending the form and hitting every enemy around it for a Bullet Dash hit times its speed just before the crash, with
 * Simon's Set's blast effects in fire colours. A giant crashes when its nose reaches a block, however far back its
 * rider is. Once armed it flies on past its time until it crashes, or blows up where it is if detonationOvertimeTicks
 * more go by first. Clients draw the Bill in place of the player, with its smoke trail.
 * The rarer helmets stand in for the set's helmet and change the Bill: the Banzai Bill Helmet draws it five times larger
 * and the King Bill Helmet ten times, light red and radiating heat that burns the enemies around it. The size carries
 * over to Bullet Dash's reach and the detonation's radius; the collision box stays small enough for vault corridors.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class BulletBill {
    private static final int DASH_HIT_TICKS = 20;

    private static final float THRUST_PER_UPGRADE = 0.3F;
    private static final int RAMP_TICKS = 20;
    private static final String JETS_MOD = "pneumaticcraft";
    private static final int BLAST_COLOR = 0xFF6A1E;
    private static final double NOSE_REACH = 0.93;
    private static final ResourceLocation SET = WoldsVaults.id("bullet_bill");

    public static final int VARIANT_BULLET = 0;
    public static final int VARIANT_BANZAI = 1;
    public static final int VARIANT_KING = 2;
    private static final ResourceLocation BANZAI_HELMET = WoldsVaults.id("banzai_bill_helmet");
    private static final ResourceLocation KING_HELMET = WoldsVaults.id("king_bill_helmet");

    private static final Map<UUID, Form> FORMS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> READY = new HashMap<>();
    private static final Map<UUID, BoostedDash> BOOSTED_DASHES = new HashMap<>();
    private static final Set<UUID> SAFE_LANDINGS = new HashSet<>();

    public static boolean inForm(ServerPlayer player) {
        return FORMS.containsKey(player.getUUID());
    }

    private static final class Form {
        final long endTick;
        final Tuning tuning;
        final BulletBillConfig config;
        final Flight flight;
        final float dashGrant;
        final float detonationGrant;
        boolean armed;

        Form(long endTick, BulletBillConfig config, float dashGrant, float detonationGrant, int variant) {
            this.endTick = endTick;
            this.tuning = Tuning.of(config, detonationGrant > 0.0F, variant);
            this.flight = new Flight(this.tuning);
            this.config = config;
            this.dashGrant = dashGrant;
            this.detonationGrant = detonationGrant;
        }
    }

    /** A Bullet Dash cast as a Bill: its hits until endTick deal this much more */
    private record BoostedDash(long endTick, float multiplier) {
    }

    /**
     * The flight as both sides keep it: whether jump is firing the jets, the boost left, how many ticks the jets have
     * opened up by, the boots doing the flying (once this side has flown it), and the speed the Bill was last sent at,
     * which is what it's doing when it hits something.
     */
    public static final class Flight {
        public boolean thrusting;
        public int boostLeft;
        int thrustTicks;
        @Nullable
        BillJets jets;
        double lastSpeed;

        /** Fired out of the cannon at its cruising speed */
        public Flight(Tuning tuning) {
            this.lastSpeed = tuning.cruise();
        }
    }

    /**
     * What a client needs to fly and draw a Bill the way the server does, whether right click arms it, and which Bill
     * it is and how many times its usual size.
     */
    public record Tuning(int upgrades, float power, boolean stabilizers, boolean smartHover, int boostTicks, float boostSpeed, int spinTicks, boolean detonation,
                         int variant, float scale, float cruise) {
        static Tuning of(BulletBillConfig config, boolean detonation, int variant) {
            float scale = variant == VARIANT_KING ? config.kingScale : variant == VARIANT_BANZAI ? config.banzaiScale : 1.0F;
            return new Tuning(Math.max(1, config.jetUpgrades), Mth.clamp(config.jetPower, 0.0F, 1.0F), config.flightStabilizers, config.smartHover,
                    Math.max(1, config.boostTicks), config.boostSpeed, Math.max(1, config.spinTicks), detonation, variant, Math.max(1.0F, scale), Math.max(0.0F, config.cruiseSpeed));
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(this.upgrades);
            buffer.writeFloat(this.power);
            buffer.writeBoolean(this.stabilizers);
            buffer.writeBoolean(this.smartHover);
            buffer.writeVarInt(this.boostTicks);
            buffer.writeFloat(this.boostSpeed);
            buffer.writeVarInt(this.spinTicks);
            buffer.writeBoolean(this.detonation);
            buffer.writeVarInt(this.variant);
            buffer.writeFloat(this.scale);
            buffer.writeFloat(this.cruise);
        }

        public static Tuning read(FriendlyByteBuf buffer) {
            return new Tuning(buffer.readVarInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(),
                    buffer.readVarInt(), buffer.readFloat(), buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(), buffer.readFloat(), buffer.readFloat());
        }
    }

    private BulletBill() {
    }

    private static AttributeSnapshot snapshot(Player player) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player);
    }

    /**
     * One tick of the jets, at the end of the player's tick: the boots do the flying, at the power set for this moment.
     */
    public static void fly(Player player, Flight flight, Tuning tuning) {
        player.getAbilities().flying = false;
        ((PlayerJumpAccessor) player).woldsvaults$setJumpTriggerTime(0);
        boolean firing = flight.thrusting || flight.boostLeft > 0;
        boolean running = firing || tuning.cruise() > 0.0F;
        if (running && player.isOnGround() && player.getLookAngle().y > 0.0) {
            player.setOnGround(false);
        }
        jets(player, flight, tuning).tick(running, power(flight, tuning));
        player.fallDistance = 0.0F;
        flight.thrustTicks = firing ? Math.min(RAMP_TICKS, flight.thrustTicks + 1) : Math.max(0, flight.thrustTicks - 1);
        if (flight.boostLeft > 0) {
            flight.boostLeft--;
        }
        flight.lastSpeed = player.getDeltaMovement().length();
    }

    /**
     * The boots' power setting right now. With a cruise, the jets open from that up to full as they spool; with none,
     * the boots' own spool-up does the same from nothing. A Bullet Dash pushes them past full.
     */
    private static float power(Flight flight, Tuning tuning) {
        float open = tuning.power();
        if (tuning.cruise() > 0.0F) {
            float cruise = Math.min(tuning.cruise() / (THRUST_PER_UPGRADE * tuning.upgrades()), open);
            open = cruise + (open - cruise) * flight.thrustTicks / RAMP_TICKS;
        }
        return open * boost(flight, tuning);
    }

    /**
     * How much faster a Bullet Dash makes the Bill: the full boostSpeed, easing back to 1 over the boost's last third
     */
    private static float boost(Flight flight, Tuning tuning) {
        if (flight.boostLeft <= 0) {
            return 1.0F;
        }
        float strength = Math.min(1.0F, flight.boostLeft / (tuning.boostTicks() / 3.0F));
        return 1.0F + (tuning.boostSpeed() - 1.0F) * strength;
    }

    /** A Bullet Dash firing the jets: at full speed at once, with no ease-in, on either side. */
    public static void startBoost(Player player, Flight flight, Tuning tuning, int ticks) {
        flight.boostLeft = ticks;
        flight.thrustTicks = RAMP_TICKS;
        jets(player, flight, tuning).spoolUp();
    }

    private static BillJets jets(Player player, Flight flight, Tuning tuning) {
        if (flight.jets == null) {
            flight.jets = new BillJets(player, tuning);
        }
        return flight.jets;
    }

    /**
     * A double tap of jump: makes the player a Bill if the set allows it and the cooldown has run. A wearer still
     * waiting is told how long is left. In creative the same double tap has just started vanilla flight, which the form
     * replaces.
     */
    public static void transform(ServerPlayer player) {
        if (FORMS.containsKey(player.getUUID())) {
            return;
        }
        BulletBillConfig config = BulletBillConfig.installed();
        int pieces = piecesCounted(player);
        int ticks = (int) GearSetHelper.tierGrant(SET, pieces, ModGearAttributes.BULLET_BILL);
        if (config == null || ticks <= 0 || !ModList.get().isLoaded(JETS_MOD) || !player.isAlive() || player.isSpectator() || player.isPassenger()) {
            return;
        }
        long now = player.server.getTickCount();
        long ready = READY.getOrDefault(player.getUUID(), 0L);
        if (now < ready) {
            player.displayClientMessage(new TextComponent("Bullet Bill ready in " + (int) Math.ceil((ready - now) / 20.0) + "s"), true);
            return;
        }
        if (player.isFallFlying()) {
            player.stopFallFlying();
        }
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        float dash = GearSetHelper.tierGrant(SET, pieces, ModGearAttributes.BULLET_BILL_DASH);
        float detonation = GearSetHelper.tierGrant(SET, pieces, ModGearAttributes.BULLET_BILL_DETONATION);
        Form form = new Form(now + ticks, config, dash, detonation, variantOf(player));
        FORMS.put(player.getUUID(), form);
        SAFE_LANDINGS.remove(player.getUUID());
        player.fallDistance = 0.0F;
        fly(player, form.flight, form.tuning);
        send(player, new BulletBillMessage(player.getId(), BulletBillMessage.START, ticks, form.tuning));

        ServerLevel level = player.getLevel();
        Vec3 at = player.position().add(0.0, 0.5, 0.0);
        Vec3 back = at.subtract(player.getLookAngle().scale(1.0));
        level.sendParticles(ParticleTypes.POOF, back.x, back.y, back.z, 20, 0.3, 0.3, 0.3, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, back.x, back.y, back.z, 8, 0.2, 0.2, 0.2, 0.02);
        sound(level, at, form, SoundEvents.GENERIC_EXPLODE, 0.6F, 1.7F);
        sound(level, at, form, SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0F, 0.6F);
    }

    /**
     * The client's jump key: fires or cuts the jets, and tells whoever else can see the Bill (its rider already knows).
     */
    public static void thrust(ServerPlayer player, boolean on) {
        Form form = FORMS.get(player.getUUID());
        if (form == null || form.flight.thrusting == on) {
            return;
        }
        form.flight.thrusting = on;
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                new BulletBillMessage(player.getId(), BulletBillMessage.THRUST, on ? 1 : 0, null));
    }

    /** How many pieces of the set the wearer has on */
    private static int piecesCounted(ServerPlayer player) {
        Set<ResourceLocation> worn = GearSetHelper.wornPieces(snapshot(player)).get(SET);
        return worn == null ? 0 : worn.size();
    }

    /** Right click as a Bill: with the detonation grant, the next crash at speed blows it up. */
    public static void arm(ServerPlayer player) {
        Form form = FORMS.get(player.getUUID());
        if (form == null || form.armed) {
            return;
        }
        if (!form.tuning.detonation()) {
            player.displayClientMessage(new TextComponent("Detonation needs both Bullet Bill pieces"), true);
            return;
        }
        form.armed = true;
        send(player, new BulletBillMessage(player.getId(), BulletBillMessage.ARM, 0, null));
        player.displayClientMessage(new TextComponent("Detonation armed"), true);
        sound(player.getLevel(), player.position(), form, SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
    }

    /**
     * The crash: every enemy in the blast takes a Bullet Dash hit times the speed (blocks a tick) times the grant. A
     * faster crash is also a wider blast with more going on in it.
     */
    private static void detonate(ServerPlayer player, Form form, double speed) {
        float damage = dashHit(player, form) * (float) speed * form.detonationGrant;
        float radius = Math.min(form.config.detonationMaxRadius, form.config.detonationRadius + form.config.detonationRadiusPerSpeed * (float) speed) * reachOf(form);
        ServerLevel level = player.getLevel();
        Vec3 c = player.getBoundingBox().getCenter();
        if (damage > 0.0F) {
            for (Mob mob : GearSetHelper.enemiesNear(level, c, radius)) {
                RiptideDerivedHits.queue(player, mob, damage);
            }
        }
        ModNetwork.sendNear(level, c, 64.0 + radius * 3.0,
                new SimonFxMessage(SimonFxMessage.Kind.BLAST, c.x, c.y, c.z, Direction.UP, Math.min(3, (int) (speed / 2.0)), BLAST_COLOR, radius));
        sound(level, c, form, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F);
        sound(level, c, form, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 2.0F, 0.7F);
        sound(level, c, form, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.2F, 1.3F);
        end(player, false);
    }

    /** How many times further a Bill's dash and blast reach: its extra size, or the share of it the config allows */
    private static float reachOf(Form form) {
        return 1.0F + (form.tuning.scale() - 1.0F) * Math.max(0.0F, form.config.giantReachShare);
    }

    /** What Bullet Dash's reach from this player is multiplied by right now: 1 unless they're a giant Bill. */
    public static float reachMultiplier(ServerPlayer player) {
        Form form = FORMS.get(player.getUUID());
        return form == null ? 1.0F : reachOf(form);
    }

    /** Which helmet the wearer has on decides which Bill they become */
    private static int variantOf(ServerPlayer player) {
        List<ResourceLocation> worn = snapshot(player).getAttributeValueList(iskallia.vault.init.ModGearAttributes.UNIQUE_ITEM_KEY);
        return worn.contains(KING_HELMET) ? VARIANT_KING : worn.contains(BANZAI_HELMET) ? VARIANT_BANZAI : VARIANT_BULLET;
    }

    /**
     * A King Bill's heat: every so often, each enemy around it burns for its share of a Bullet Dash hit per second.
     */
    private static void radiate(ServerPlayer player, Form form) {
        int interval = Math.max(1, form.config.kingHeatIntervalTicks);
        if (player.tickCount % interval != 0) {
            return;
        }
        float damage = dashHit(player, form) * form.config.kingHeatDamage * interval / 20.0F;
        ServerLevel level = player.getLevel();
        Vec3 c = player.getBoundingBox().getCenter();
        for (Mob mob : GearSetHelper.enemiesNear(level, c, form.config.kingHeatRadius)) {
            RiptideDerivedHits.queue(player, mob, damage);
            Vec3 at = mob.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 5, mob.getBbWidth() * 0.4, mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.4, 0.01);
        }
        if (player.tickCount % 20 < interval) {
            sound(level, c, form, SoundEvents.BLAZE_BURN, 0.8F, 0.6F);
        }
    }

    /**
     * What one Bullet Dash hit from this player does right now, as a Bill: attack damage times the learned tier's share
     * (detonationNoDashPercent if they haven't learned Bullet Dash), with their damage multipliers and the set's dash
     * bonus.
     */
    private static float dashHit(ServerPlayer player, Form form) {
        float percent = form.config.detonationNoDashPercent;
        for (SpecializedSkill skill : PlayerAbilitiesData.get(player.getLevel()).getAbilities(player).getAll(SpecializedSkill.class, candidate -> true)) {
            if (skill.getSpecialization() instanceof TieredSkill tiered && tiered.isUnlocked() && tiered.getChild() instanceof DashDamageAbility dash) {
                percent = dash.getAttackDamagePercentPerDash();
            }
        }
        return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * percent * PlayerDamageHelper.getDamageMultiplier(player, true, false) * (1.0F + form.dashGrant);
    }

    public static void end(ServerPlayer player) {
        end(player, true);
    }

    /** Back to a player: the cooldown starts, the landing after is a safe one, and clients stop drawing the Bill. */
    private static void end(ServerPlayer player, boolean puff) {
        Form form = FORMS.remove(player.getUUID());
        if (form == null) {
            return;
        }
        READY.put(player.getUUID(), player.server.getTickCount() + (long) form.config.cooldownTicks);
        form.flight.jets.stop();
        player.setForcedPose(null);
        player.fallDistance = 0.0F;
        if (!player.isOnGround()) {
            SAFE_LANDINGS.add(player.getUUID());
        }
        send(player, new BulletBillMessage(player.getId(), BulletBillMessage.END, 0, null));
        if (!puff) {
            return;
        }

        ServerLevel level = player.getLevel();
        Vec3 at = player.position().add(0.0, 0.5, 0.0);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y, at.z, 24, 0.4, 0.4, 0.4, 0.06);
        sound(level, at, form, SoundEvents.FIRE_EXTINGUISH, 0.7F, 1.3F);
    }

    /**
     * Bullet Dash cast (Vault Hunters' DashDamageAbility): as a Bill with the dash grant, it fires the jets faster for
     * a moment with a spin and a red pulse, and its hits for the next second deal the grant more
     */
    public static void bulletDash(ServerPlayer player) {
        Form form = FORMS.get(player.getUUID());
        if (form == null) {
            return;
        }
        if (form.dashGrant <= 0.0F) {
            player.displayClientMessage(new TextComponent("Bullet Dash's spin needs both Bullet Bill pieces"), true);
            return;
        }
        startBoost(player, form.flight, form.tuning, form.tuning.boostTicks());
        BOOSTED_DASHES.put(player.getUUID(), new BoostedDash(player.server.getTickCount() + DASH_HIT_TICKS, 1.0F + form.dashGrant));
        send(player, new BulletBillMessage(player.getId(), BulletBillMessage.BOOST, form.tuning.boostTicks(), null));

        ServerLevel level = player.getLevel();
        Vec3 at = player.position().add(0.0, 0.5, 0.0);
        sound(level, at, form, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 1.0F, 0.8F);
        sound(level, at, form, SoundEvents.BLAZE_SHOOT, 0.8F, 0.7F);
    }

    /** What a Bullet Dash hit from this player is multiplied by: the boost from a dash cast as a Bill, else 1. */
    public static float dashDamageMultiplier(ServerPlayer player) {
        BoostedDash dash = BOOSTED_DASHES.get(player.getUUID());
        if (dash == null) {
            return 1.0F;
        }
        if (player.server.getTickCount() >= dash.endTick()) {
            BOOSTED_DASHES.remove(player.getUUID());
            return 1.0F;
        }
        return dash.multiplier();
    }

    /** A sound at the Bill, heard further the bigger it is, since a giant's camera sits well back from it */
    private static void sound(ServerLevel level, Vec3 at, Form form, SoundEvent event, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, event, SoundSource.PLAYERS, volume * form.tuning.scale(), pitch);
    }

    private static void send(ServerPlayer player, BulletBillMessage message) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
    }

    /**
     * Whether the Bill has hit something: its rider touching a block, or its nose reaching one, which for a giant comes
     * long before its rider gets there
     */
    private static boolean crashed(ServerPlayer player, Form form) {
        if (player.horizontalCollision || player.verticalCollision) {
            return true;
        }
        Vec3 from = player.getBoundingBox().getCenter();
        Vec3 nose = from.add(player.getLookAngle().scale(NOSE_REACH * form.tuning.scale()));
        return !CrystalOptics.canSee(player.level, from, nose, player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (!SAFE_LANDINGS.isEmpty() && (player.isOnGround() || player.isInWater()) && !FORMS.containsKey(player.getUUID())) {
            SAFE_LANDINGS.remove(player.getUUID());
        }
        Form form = FORMS.get(player.getUUID());
        if (form == null) {
            return;
        }
        long now = player.server.getTickCount();
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || (!form.armed && now >= form.endTick)) {
            end(player);
            return;
        }
        if (form.armed && (now >= form.endTick + form.config.detonationOvertimeTicks
                || (form.flight.lastSpeed >= form.config.detonationMinSpeed && crashed(player, form)))) {
            detonate(player, form, form.flight.lastSpeed);
            return;
        }
        fly(player, form.flight, form.tuning);
        if (form.tuning.variant() == VARIANT_KING) {
            radiate(player, form);
        }
        ((ServerGamePacketListenerImplAccessor) player.connection).woldsvaults$setAboveGroundTickCount(0);
    }

    /** A Bill doesn't take fall damage, nor does its rider on the landing after */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player
                && (FORMS.containsKey(player.getUUID()) || SAFE_LANDINGS.remove(player.getUUID()))) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
        }
    }

    /** Someone who starts seeing a Bill is told about it, so they draw it too */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getPlayer() instanceof ServerPlayer tracker) {
            Form form = FORMS.get(target.getUUID());
            if (form != null) {
                int left = (int) Math.max(1L, form.endTick - target.server.getTickCount());
                ModNetwork.sendToClient(new BulletBillMessage(target.getId(), BulletBillMessage.START, left, form.tuning), tracker);
                if (form.flight.thrusting) {
                    ModNetwork.sendToClient(new BulletBillMessage(target.getId(), BulletBillMessage.THRUST, 1, null), tracker);
                }
                if (form.armed) {
                    ModNetwork.sendToClient(new BulletBillMessage(target.getId(), BulletBillMessage.ARM, 0, null), tracker);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            end(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            end(player);
        }
    }

    /** The cooldown is kept, so logging out and back in doesn't skip it */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getPlayer().getUUID();
        Form form = FORMS.remove(id);
        if (form != null) {
            form.flight.jets.stop();
        }
        BOOSTED_DASHES.remove(id);
        SAFE_LANDINGS.remove(id);
        if (event.getPlayer().getServer() != null) {
            long now = event.getPlayer().getServer().getTickCount();
            READY.values().removeIf(ready -> ready <= now);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FORMS.values().forEach(form -> form.flight.jets.stop());
        FORMS.clear();
        READY.clear();
        BOOSTED_DASHES.clear();
        SAFE_LANDINGS.clear();
    }
}

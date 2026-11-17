package xyz.iwolfking.woldsvaults.client.bullet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;
import xyz.iwolfking.woldsvaults.network.message.BulletBillActionMessage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * The Bullet Bill set on the client. A double tap of jump asks the server for the form. While this client's player is
 * a Bill, holding jump fires the jets, as the thrust key does for PneumaticCraft's jet boots, and it flies them each
 * tick the same way the server does; the view leans into turns as it flies, in every camera, they don't see their hands, and
 * right click asks the server to arm the Bill instead of using items. Every Bill this client knows about (its
 * own player's and the ones it sees) leaves a smoke trail while its jets fire, puffs while it hovers, and plays its
 * jets; an armed one blinks red, throws sparks and beeps, a giant one (the Banzai and King Bill helmets) trails great
 * clouds across its whole tail, and a King Bill shimmers with flames. BulletBillRenderer draws them.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientBulletBill {
    // the window for the second tap, as for vanilla's creative flight
    private static final int DOUBLE_TAP_TICKS = 7;
    // how long past its time a Bill keeps going while the server's END is on its way (an armed one waits for END however long)
    private static final int END_GRACE_TICKS = 10;
    private static final float BOOST_FOV = 1.12F;
    // the most the view rolls in a turn, in degrees: the jet boots' default
    private static final float MAX_VIEW_ROLL = 35.0F;
    // where the smoke leaves, behind the Bill's middle along its heading, in blocks
    private static final double EXHAUST = 0.8;
    // about one puff every this many blocks along the path, so a fast Bill doesn't leave gaps
    private static final double PUFF_SPACING = 0.45;
    private static final int PULSES = 4;
    // an armed Bill blinks, and beeps, once every this many ticks
    private static final int FUSE_TICKS = 6;
    private static final float SPIN_TURNS = 2.0F;

    private static final Map<Integer, Bill> BILLS = new HashMap<>();
    private static boolean jumpWasDown;
    private static int sinceJumpTap = 1000;
    private static float viewRoll;

    // One Bill this client draws, and for its own player the flight it flies.
    public static final class Bill {
        final BulletBill.Tuning tuning;
        final BulletBill.Flight flight;
        final int totalTicks;
        int ticksLeft;
        int boostTicks;
        int boostAge = -1;
        int armedAge = -1;
        boolean ended;
        // the lagging copies of its heading that the camera and the banking follow, and when they last moved
        float cameraYaw;
        float cameraPitch;
        float cameraTime = -1.0F;
        float bankYaw;
        float bankTime = -1.0F;
        @Nullable
        Vec3 lastExhaust;
        @Nullable
        BulletBillSound sound;

        Bill(BulletBill.Tuning tuning, int ticks) {
            this.tuning = tuning;
            this.flight = new BulletBill.Flight(tuning);
            this.totalTicks = ticks;
            this.ticksLeft = ticks;
        }

        public boolean boosting() {
            return this.boostAge >= 0 && this.boostAge < this.boostTicks;
        }

        public boolean firing() {
            return this.flight.thrusting || this.boosting();
        }

        // firing its jets, or cruising along
        public boolean flying() {
            return this.firing() || this.tuning.cruise() > 0.0F;
        }

        // how far through the spin a Bullet Dash sets off, 0 to 1
        private float spinProgress(float partialTick) {
            return this.boostAge < 0 ? 1.0F : Math.min(1.0F, (this.boostAge + partialTick) / this.tuning.spinTicks());
        }

        // the spin's roll, in degrees, easing out over spinTicks
        public float spin(float partialTick) {
            float t = spinProgress(partialTick);
            return t >= 1.0F ? 0.0F : 360.0F * SPIN_TURNS * (1.0F - (1.0F - t) * (1.0F - t));
        }

        // how much is going on in the spin, 0 to 1: nothing at either end of it and the most halfway through
        public float spinAmount(float partialTick) {
            float t = spinProgress(partialTick);
            return t >= 1.0F ? 0.0F : Mth.sin(t * Mth.PI);
        }

        // how red the Bill glows, 0 to 1: PULSES flashes across a boost, and a steady blink while it's armed
        public float redPulse(float partialTick) {
            float fuse = this.armedAge < 0 ? 0.0F : 0.5F - 0.5F * Mth.cos((this.armedAge + partialTick) / FUSE_TICKS * Mth.TWO_PI);
            if (!this.boosting()) {
                return fuse;
            }
            float t = (this.boostAge + partialTick) / this.boostTicks;
            return Math.max(fuse, 0.5F - 0.5F * Mth.cos(t * PULSES * Mth.TWO_PI));
        }
    }

    private ClientBulletBill() {
    }

    // ---------- from the server ----------

    public static void start(int entityId, int ticks, @Nullable BulletBill.Tuning tuning) {
        if (tuning == null || Minecraft.getInstance().level == null) {
            return;
        }
        Bill old = BILLS.put(entityId, new Bill(tuning, ticks));
        if (old != null) {
            old.ended = true;
        }
    }

    public static void boost(int entityId, int ticks) {
        Bill bill = BILLS.get(entityId);
        if (bill == null) {
            return;
        }
        bill.boostAge = 0;
        bill.boostTicks = Math.max(1, ticks);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            BulletBill.startBoost(mc.player, bill.flight, bill.tuning, bill.boostTicks);
        }
    }

    public static void arm(int entityId) {
        Bill bill = BILLS.get(entityId);
        if (bill != null && bill.armedAge < 0) {
            bill.armedAge = 0;
        }
    }

    // another player's jets firing or cutting out
    public static void thrust(int entityId, boolean on) {
        Bill bill = BILLS.get(entityId);
        if (bill != null) {
            bill.flight.thrusting = on;
        }
    }

    public static void end(int entityId) {
        Bill bill = BILLS.remove(entityId);
        if (bill != null) {
            bill.ended = true;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            mc.player.setForcedPose(null);
        }
    }

    // The Bill a player is right now, if this client knows of one.
    @Nullable
    public static Bill bill(Player player) {
        return BILLS.get(player.getId());
    }

    // ---------- ticking ----------

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            BILLS.values().forEach(bill -> bill.ended = true);
            BILLS.clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        doubleTap(mc);
        Iterator<Map.Entry<Integer, Bill>> it = BILLS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Bill> entry = it.next();
            Bill bill = entry.getValue();
            bill.ticksLeft--;
            if (bill.boostAge >= 0) {
                bill.boostAge++;
            }
            Entity entity = mc.level.getEntity(entry.getKey());
            if (!(entity instanceof Player player) || !player.isAlive() || (bill.armedAge < 0 && bill.ticksLeft < -END_GRACE_TICKS)) {
                bill.ended = true;
                it.remove();
                if (entity == mc.player) {
                    mc.player.setForcedPose(null);
                }
                continue;
            }
            trail(mc, player, bill);
            if (bill.armedAge >= 0 && bill.armedAge++ % FUSE_TICKS == 0) {
                // the rider hears the fuse at the camera, however far back it sits
                Vec3 at = player == mc.player ? mc.gameRenderer.getMainCamera().getPosition() : player.position();
                mc.level.playLocalSound(at.x, at.y, at.z, SoundEvents.NOTE_BLOCK_BIT, SoundSource.PLAYERS, player == mc.player ? 0.7F : 0.7F * bill.tuning.scale(), 1.7F, false);
            }
            // the flight sound starts up whenever it's flying or holding a hover, and winds down by itself on the ground
            if ((bill.flying() || !player.isOnGround()) && (bill.sound == null || bill.sound.isStopped())) {
                bill.sound = new BulletBillSound(player, bill);
                mc.getSoundManager().play(bill.sound);
            }
        }
    }

    // a second press of jump within DOUBLE_TAP_TICKS of the first asks for the form; a third press starts over. Asked in
    // creative too, where the server turns vanilla's flight back off if the set makes the player a Bill. Spectators
    // keep the double tap for themselves, and a Bill's jump key is its thrust
    private static void doubleTap(Minecraft mc) {
        LocalPlayer player = mc.player;
        boolean down = mc.options.keyJump.isDown();
        sinceJumpTap = Math.min(sinceJumpTap + 1, 1000);
        if (down && !jumpWasDown && mc.screen == null && !BILLS.containsKey(player.getId())) {
            if (sinceJumpTap <= DOUBLE_TAP_TICKS && !player.isSpectator() && !player.isPassenger()) {
                ModNetwork.sendToServer(new BulletBillActionMessage(BulletBillActionMessage.Action.TRANSFORM));
                sinceJumpTap = 1000;
            } else {
                sinceJumpTap = 0;
            }
        }
        jumpWasDown = down;
    }

    // this client's own Bill, at the end of the player's tick as on the server: jump fires the jets, then they run
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.CLIENT || event.player != Minecraft.getInstance().player) {
            return;
        }
        Bill bill = BILLS.get(event.player.getId());
        if (bill == null) {
            return;
        }
        boolean held = Minecraft.getInstance().options.keyJump.isDown();
        if (held != bill.flight.thrusting) {
            ModNetwork.sendToServer(new BulletBillActionMessage(held ? BulletBillActionMessage.Action.THRUST_ON : BulletBillActionMessage.Action.THRUST_OFF));
            bill.flight.thrusting = held;
        }
        BulletBill.fly(event.player, bill.flight, bill.tuning);
    }

    // white puffs out of the tail while the jets fire, filling the path since last tick, with sparks during a boost;
    // a hovering Bill only lets out a puff now and then, downwards
    private static void trail(Minecraft mc, Player player, Bill bill) {
        float scale = bill.tuning.scale();
        Vec3 heading = player.getLookAngle();
        Vec3 middle = player.position().add(0.0, BulletBillRenderer.center(bill), 0.0);
        Vec3 exhaust = middle.subtract(heading.scale(EXHAUST * scale));
        Random random = player.getRandom();
        if (bill.tuning.variant() == BulletBill.VARIANT_KING) {
            for (int i = 0; i < 6; i++) {
                Vec3 at = middle.add((random.nextDouble() - 0.5) * scale, (random.nextDouble() - 0.5) * scale, (random.nextDouble() - 0.5) * scale);
                mc.level.addParticle(i == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME, at.x, at.y, at.z, 0.0, 0.03, 0.0);
            }
        }
        if (bill.armedAge >= 0) {
            mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, exhaust.x + (random.nextDouble() - 0.5) * 0.4, exhaust.y + (random.nextDouble() - 0.5) * 0.4,
                    exhaust.z + (random.nextDouble() - 0.5) * 0.4, 0.0, 0.05, 0.0);
            mc.level.addParticle(ParticleTypes.SMALL_FLAME, exhaust.x, exhaust.y, exhaust.z, 0.0, 0.02, 0.0);
        }
        if (!bill.flying()) {
            bill.lastExhaust = null;
            if (!player.isOnGround() && (player.tickCount & 3) == 0) {
                mc.level.addParticle(ParticleTypes.CLOUD, exhaust.x, exhaust.y - 0.25, exhaust.z, 0.0, -0.1, 0.0);
            }
            return;
        }
        Vec3 from = bill.lastExhaust == null ? exhaust : bill.lastExhaust;
        bill.lastExhaust = exhaust;
        int puffs = Mth.clamp((int) Math.ceil(from.distanceTo(exhaust) / PUFF_SPACING), 1, 12);
        Vec3 drift = heading.scale(-0.03);
        for (int i = 1; i <= puffs; i++) {
            Vec3 at = from.lerp(exhaust, (double) i / puffs);
            mc.level.addParticle(ParticleTypes.CLOUD, at.x + (random.nextDouble() - 0.5) * 0.2, at.y + (random.nextDouble() - 0.5) * 0.2,
                    at.z + (random.nextDouble() - 0.5) * 0.2, drift.x, drift.y + 0.01, drift.z);
        }
        if (player.tickCount % 3 == 0) {
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, exhaust.x, exhaust.y, exhaust.z, drift.x, drift.y, drift.z);
        }
        // a giant's tail is too wide for one stream of puffs: big clouds billow out all across it
        for (int i = 0; i < (int) Math.min(scale, 12.0F) && scale > 1.0F; i++) {
            Vec3 at = exhaust.add((random.nextDouble() - 0.5) * scale * 0.7, (random.nextDouble() - 0.5) * scale * 0.7, (random.nextDouble() - 0.5) * scale * 0.7);
            mc.level.addParticle(i % 3 == 0 ? ParticleTypes.EXPLOSION : ParticleTypes.CLOUD, at.x, at.y, at.z, drift.x, drift.y, drift.z);
        }
        if (bill.boosting()) {
            for (int i = 0; i < 3; i++) {
                mc.level.addParticle(ParticleTypes.FLAME, exhaust.x + (random.nextDouble() - 0.5) * 0.3, exhaust.y + (random.nextDouble() - 0.5) * 0.3,
                        exhaust.z + (random.nextDouble() - 0.5) * 0.3, heading.x * -0.08, heading.y * -0.08, heading.z * -0.08);
            }
        }
    }

    // ---------- the rider's view ----------

    // the view leans into turns the whole time a Bill flies and eases level again otherwise, as in jet boots; the faster
    // the turn, the quicker it leans. Added to the roll, so other camera effects still show
    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Player player = event.getCamera().getEntity() instanceof Player camera ? camera : null;
        Bill bill = player == null ? null : BILLS.get(player.getId());
        // a form ending in the air still eases level; anywhere else there's nothing to do
        if (player == null || player.isOnGround() || (bill == null && Math.abs(viewRoll) < 0.01F)) {
            viewRoll = 0.0F;
            return;
        }
        float target = 0.0F;
        float divisor = 50.0F;
        float turn = player.yHeadRot - player.yHeadRotO;
        if (bill != null && bill.flying() && Math.abs(turn) >= 1.0E-4F) {
            target = Math.signum(turn) * MAX_VIEW_ROLL;
            // below 1 a sharp flick would overshoot the target and leave the view tilted far past it for seconds
            divisor = Math.max(1.0F, Math.abs(100.0F / turn));
        }
        viewRoll += (target - viewRoll) / divisor;
        event.setRoll(event.getRoll() + viewRoll);
    }

    // right click arms the detonation instead of using whatever's in hand; the server says so if the set doesn't allow it
    @SubscribeEvent
    public static void onClick(InputEvent.ClickInputEvent event) {
        Player player = Minecraft.getInstance().player;
        Bill bill = player == null ? null : BILLS.get(player.getId());
        if (bill == null || !event.isUseItem()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (bill.armedAge < 0) {
            ModNetwork.sendToServer(new BulletBillActionMessage(BulletBillActionMessage.Action.ARM));
        }
    }

    // a Bill has no hands to show
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && BILLS.containsKey(player.getId())) {
            event.setCanceled(true);
        }
    }

    // a little wider view while a Bullet Dash boost lasts; vanilla eases it in and out
    @SubscribeEvent
    public static void onFov(FOVModifierEvent event) {
        Bill bill = BILLS.get(event.getEntity().getId());
        if (bill != null && bill.boosting() && event.getEntity() == Minecraft.getInstance().player) {
            event.setNewfov(event.getNewfov() * BOOST_FOV);
        }
    }
}

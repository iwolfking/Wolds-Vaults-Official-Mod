package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;
import xyz.iwolfking.woldsvaults.network.message.LightningAbilityMessage;
import xyz.iwolfking.woldsvaults.network.message.LightningDashMessage;

import java.util.List;

/**
 * The local player's lightning form (Lightning Fiend 6-piece) on the client: while it lasts, a fresh press of a
 * movement key asks the server for a dash that way, jump for one up and sneak for one down (neither jumps or
 * crouches meanwhile). Holding a key doesn't repeat; the server re-checks everything.
 * Once the server starts the dash, this carries the player along its path a step each tick, so the camera glides
 * there as the player crosses it as lightning, rather than jumping.
 * Attack, use and pick block don't attack, use items or pick blocks while the form lasts: holding attack keeps
 * lashing with Arc Lash, a press of use marks a Lightning Rod target, aimed at whatever is under the crosshair, and a
 * press of pick block (middle click) calls down the Thunderstrike that ends the form.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientLightningForm {
    private static final float DASH_FOV = 1.15F;
    private static final double AIM_RANGE = 32.0;

    private static int formTicks;
    private static int remaining;
    private static int cooldownTicks;
    private static int sinceDash;
    private static final boolean[] WAS_DOWN = new boolean[LightningFiend.Dash.values().length];

    private static int lashCooldownTicks;
    private static int sinceLash;
    private static int rodCooldownTicks;
    private static int sinceRod;
    private static boolean useWasDown;
    private static boolean pickWasDown;
    private static int markId = -1;

    private static List<Vec3> path;
    private static Level travelLevel;
    private static int travelTicks;
    private static int travelAge;
    private static int sentStep;

    private ClientLightningForm() {
    }

    public static void start(int ticks, int dashCooldownTicks, int lashCooldown, int rodCooldown) {
        formTicks = ticks;
        remaining = ticks;
        cooldownTicks = dashCooldownTicks;
        sinceDash = dashCooldownTicks;
        lashCooldownTicks = lashCooldown;
        sinceLash = lashCooldown;
        rodCooldownTicks = rodCooldown;
        sinceRod = rodCooldown;
        markId = -1;
        Options options = Minecraft.getInstance().options;
        KeyMapping[] keys = keys(options);
        for (int i = 0; i < keys.length; i++) {
            WAS_DOWN[i] = keys[i].isDown();
        }
        useWasDown = options.keyUse.isDown();
        pickWasDown = options.keyPickItem.isDown();
    }

    /** A dash the server started, by this player or another: draw it, and if it's this player's, carry them along. */
    public static void dash(int entityId, List<Vec3> dashPath, int ticks, long seed) {
        LightningDashRenderer.add(entityId, dashPath, ticks, seed);
        Player player = Minecraft.getInstance().player;
        if (player != null && player.getId() == entityId) {
            path = dashPath;
            travelLevel = player.level;
            travelTicks = ticks;
            travelAge = 0;
            sentStep = 0;
            LightningSpeedLines.start(ticks, dashPath.get(dashPath.size() - 1).subtract(dashPath.get(0)));
        }
    }

    /** The server marked a Lightning Rod target (or the mark lapsed, -1); a new mark starts the rod's cooldown. */
    public static void mark(int entityId) {
        markId = entityId;
        if (entityId >= 0) {
            sinceRod = 0;
        }
    }

    public static boolean isTravelling() {
        return path != null;
    }

    /** Whether the server has told this client its player is lightning, and the form hasn't run out. */
    public static boolean isLightning(Player player) {
        return player == Minecraft.getInstance().player && isActive();
    }

    static boolean isActive() {
        return remaining > 0 || path != null;
    }

    static float formLeft(float partialTick) {
        return formTicks <= 0 ? 0.0F : Math.max(0.0F, (remaining - partialTick) / formTicks);
    }

    static float lashCooldown(float partialTick) {
        return cooldown(sinceLash, lashCooldownTicks, partialTick);
    }

    static float rodCooldown(float partialTick) {
        return cooldown(sinceRod, rodCooldownTicks, partialTick);
    }

    private static float cooldown(int since, int ticks, float partialTick) {
        return ticks <= 0 ? 0.0F : Math.max(0.0F, 1.0F - (since + partialTick) / ticks);
    }

    static int markId() {
        return markId;
    }

    private static KeyMapping[] keys(Options options) {
        return new KeyMapping[] { options.keyUp, options.keyLeft, options.keyRight, options.keyDown, options.keyJump, options.keyShift };
    }

    private static float dashYaw(Minecraft mc) {
        if (mc.player == null || mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            return mc.player == null ? 0.0F : mc.player.getYRot();
        }
        return mc.gameRenderer.getMainCamera().getYRot();
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (isLightning(event.getPlayer())) {
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public static void onClick(InputEvent.ClickInputEvent event) {
        if (isActive() && Minecraft.getInstance().player != null) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            remaining = 0;
            path = null;
            markId = -1;
            return;
        }
        if (remaining <= 0) {
            markId = -1;
            return;
        }
        remaining--;
        if (path == null) {
            sinceDash++;
        }
        sinceLash++;
        sinceRod++;
        KeyMapping[] keys = keys(mc.options);
        LightningFiend.Dash[] dashes = LightningFiend.Dash.values();
        for (int i = 0; i < keys.length; i++) {
            boolean down = keys[i].isDown();
            if (down && !WAS_DOWN[i] && path == null && mc.screen == null && sinceDash >= cooldownTicks) {
                ModNetwork.sendToServer(new LightningDashMessage(dashes[i], dashYaw(mc)));
            }
            WAS_DOWN[i] = down;
        }

        boolean useDown = mc.options.keyUse.isDown();
        boolean pickDown = mc.options.keyPickItem.isDown();
        if (mc.screen == null) {
            if (mc.options.keyAttack.isDown() && sinceLash >= lashCooldownTicks) {
                ability(mc, LightningAbilityMessage.ARC_LASH);
                sinceLash = 0;
            }
            if (useDown && !useWasDown && sinceRod >= rodCooldownTicks) {
                ability(mc, LightningAbilityMessage.LIGHTNING_ROD);
            }
            if (pickDown && !pickWasDown && path == null) {
                ability(mc, LightningAbilityMessage.THUNDERSTRIKE);
            }
        }
        useWasDown = useDown;
        pickWasDown = pickDown;
    }

    private static void ability(Minecraft mc, int ability) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        ModNetwork.sendToServer(new LightningAbilityMessage(ability, crosshairTarget(mc, camera), new Vec3(look.x(), look.y(), look.z())));
    }

    private static int crosshairTarget(Minecraft mc, Camera camera) {
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return -1;
        }
        Vector3f lookVector = camera.getLookVector();
        Vec3 look = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        Vec3 from = camera.getPosition();
        Vec3 to = from.add(look.scale(AIM_RANGE + from.distanceTo(player.getEyePosition())));
        HitResult blocks = mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blocks.getType() != HitResult.Type.MISS) {
            to = blocks.getLocation();
        }
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, from, to, new AABB(from, to).inflate(1.0),
                entity -> entity != player && entity instanceof LivingEntity && entity.isPickable() && !entity.isSpectator(), from.distanceToSqr(to));
        return hit == null ? -1 : hit.getEntity().getId();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.CLIENT || path == null || event.player != Minecraft.getInstance().player) {
            return;
        }
        Player player = event.player;
        if (!player.isAlive() || player.level != travelLevel) {
            path = null;
            return;
        }
        travelAge++;
        float progress = (float) travelAge / travelTicks;
        if (player instanceof LocalPlayer local) {
            int reached = (int) (Math.min(1.0F, progress) * (path.size() - 1));
            for (; sentStep < reached; sentStep++) {
                Vec3 step = path.get(sentStep + 1);
                local.connection.send(new ServerboundMovePlayerPacket.Pos(step.x, step.y, step.z, local.isOnGround()));
            }
        }
        Vec3 at = LightningFiend.along(path, progress);
        player.setPos(at.x, at.y, at.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        if (travelAge >= travelTicks) {
            path = null;
            sinceDash = 0;
        }
    }

    @SubscribeEvent
    public static void onFov(FOVModifierEvent event) {
        if (path != null && event.getEntity() == Minecraft.getInstance().player) {
            event.setNewfov(event.getNewfov() * DASH_FOV);
        }
    }
}

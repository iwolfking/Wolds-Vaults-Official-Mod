package xyz.iwolfking.woldsvaults.modifiers.gear.ancient;

import com.mojang.math.Vector3f;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.EntityHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Detects a player slamming into terrain mid-riptide and answers it with a damage wave.
 *
 * <p>Both of the obvious signals for this are unusable on a dedicated server. A player's movement is
 * client authoritative: the server applies the position the client reports, so
 * {@code getDeltaMovement} is never refreshed from ordinary movement and stays whatever the server
 * last wrote, and {@code horizontalCollision} comes back false because the client already resolved
 * the collision before reporting the position it settled at. Neither is a bug in Forge - it is just
 * that those fields only mean something for server-driven entities.</p>
 *
 * <p>So speed is measured from the change in {@link net.minecraft.world.entity.Entity#position()}
 * between ticks, which the server does keep current, and the wall is found by casting a ray along
 * the direction of travel. Both are computed entirely server side and neither depends on a value the
 * client owns.</p>
 */
@Mod.EventBusSubscriber
public class CrashwaveTracker {
    private static final Map<UUID, Vec3> lastPosition = new HashMap<>();
    private static final Map<UUID, Integer> nextAllowedTick = new HashMap<>();
    private static final float REFERENCE_SPEED = 6.0F;
    private static final float TRIDENT_DAMAGE_MULTIPLIER = 2.0F;
    private static final int RETRIGGER_COOLDOWN_TICKS = 10;
    private static final double MIN_LOOKAHEAD = 0.9;
    private static final double LOOKAHEAD_FACTOR = 1.3;
    private static final Vector3f EMBER_HOT = new Vector3f(1.0F, 0.3F, 0.08F);
    private static final Vector3f EMBER_DEEP = new Vector3f(0.72F, 0.06F, 0.04F);

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        lastPosition.clear();
        nextAllowedTick.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        lastPosition.remove(event.getPlayer().getUUID());
        nextAllowedTick.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player) || player.level.isClientSide) {
            return;
        }

        UUID id = player.getUUID();
        if (!player.isAutoSpinAttack()) {
            lastPosition.remove(id);
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof VaultTridentItem) || !VaultTridentItem.isVaultTridentRiptide(stack)) {
            lastPosition.remove(id);
            return;
        }
        if (!AttributeSnapshotHelper.canHaveSnapshot(player)) {
            return;
        }

        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        int baseRadius = snapshot.getAttributeValue(ModGearAttributes.CRASHWAVE, VaultGearAttributeTypeMerger.intSum());
        if (baseRadius <= 0) {
            lastPosition.remove(id);
            return;
        }

        Vec3 position = player.position();
        Vec3 previous = lastPosition.put(id, position);
        if (previous == null) {
            return;
        }

        Vec3 travel = position.subtract(previous);
        double perTick = travel.length();
        float impactSpeed = (float) (perTick * 20.0);
        if (impactSpeed < REFERENCE_SPEED) {
            return;
        }

        int currentTick = player.getLevel().getServer().getTickCount();
        if (currentTick < nextAllowedTick.getOrDefault(id, 0)) {
            return;
        }

        Vec3 impact = findImpactPoint(player, position, travel, perTick);
        if (impact == null) {
            return;
        }

        nextAllowedTick.put(id, currentTick + RETRIGGER_COOLDOWN_TICKS);
        triggerCrashwave(player, stack, baseRadius, impactSpeed, impact);
    }

    /**
     * Casts along the direction of travel and returns where the player is about to bury themselves in
     * terrain, or null if the path ahead is clear. The ray reaches a little further than the distance
     * covered this tick so the wave lands on the tick of contact rather than the tick after it, when
     * the player has already been stopped and their speed has collapsed. Fluids are ignored, so
     * plunging into water is not a crash.
     */
    private static Vec3 findImpactPoint(ServerPlayer player, Vec3 position, Vec3 travel, double perTick) {
        Vec3 from = position.add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 to = from.add(travel.normalize().scale(Math.max(MIN_LOOKAHEAD, perTick * LOOKAHEAD_FACTOR)));
        BlockHitResult hit = player.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : null;
    }

    /**
     * Emits the wave. Damage scales with the square root of the speed ratio and the radius with its
     * cube root, so a crash twice as fast hits ~1.41x harder over ~1.26x the radius.
     *
     * <p>The damage is dealt inside IS_SHOCKING_COLLISION rather than IS_AOE_ATTACKING on purpose.
     * Both flags stop the wave from proccing lucky hit, but PlayerDamageHelper skips its multiplier
     * pass whenever IS_AOE_ATTACKING is set, which would strip the wave of Rampage and every other
     * +% damage source the wave is meant to benefit from.</p>
     */
    private static void triggerCrashwave(ServerPlayer player, ItemStack trident, int baseRadius, float impactSpeed, Vec3 impact) {
        float speedRatio = impactSpeed / REFERENCE_SPEED;
        double tridentDamage = VaultGearData.read(trident).get(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, VaultGearAttributeTypeMerger.doubleSum());
        float damage = (float) (TRIDENT_DAMAGE_MULTIPLIER * tridentDamage * Math.sqrt(speedRatio));
        float radius = (float) (baseRadius * Math.cbrt(speedRatio));

        List<LivingEntity> targets = EntityHelper.getNearby(player.level, player.blockPosition(), radius, LivingEntity.class);
        targets.remove(player);
        targets.removeIf(entity -> entity instanceof EternalEntity || entity instanceof PetEntity);

        DamageSource source = DamageSource.playerAttack(player);
        targets.removeIf(entity -> entity.isInvulnerableTo(source));
        if (!targets.isEmpty()) {
            ActiveFlags.IS_SHOCKING_COLLISION.runIfNotSet(() -> targets.forEach(entity -> entity.hurt(source, damage)));
        }

        ServerLevel level = player.getLevel();
        spawnCrashParticles(level, impact, radius);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 1.3F);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 0.7F);
    }

    /**
     * One instantaneous burst of embers flung outward from the impact point.
     *
     * <p>Every particle is spawned with count zero, which is what lets the velocity be aimed exactly
     * rather than scattered randomly: the client reads the delta arguments as a velocity vector and
     * the speed argument as its multiplier. Directions are laid out around a ring with only a little
     * vertical spread, so the burst reads as a flat shockwave punching outward rather than a ball of
     * fire, and the dust particles bleed off speed quickly enough that it is gone almost at once.</p>
     */
    private static void spawnCrashParticles(ServerLevel level, Vec3 impact, float radius) {
        DustParticleOptions hotDust = new DustParticleOptions(EMBER_HOT, 1.3F);
        DustParticleOptions deepDust = new DustParticleOptions(EMBER_DEEP, 1.7F);
        int points = Mth.clamp((int) Math.ceil(radius * 9.0F), 28, 56);
        double burstSpeed = 0.75 + 0.06 * radius;

        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0 * i / points;
            double lift = (level.getRandom().nextDouble() - 0.4) * 0.55;
            Vec3 dir = new Vec3(Math.cos(angle), lift, Math.sin(angle)).normalize();
            level.sendParticles(i % 2 == 0 ? hotDust : deepDust, impact.x, impact.y, impact.z, 0, dir.x, dir.y, dir.z, burstSpeed);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y, impact.z, 0, dir.x, dir.y, dir.z, burstSpeed * 1.15);
            }
        }
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2.0 * i / 6.0;
            level.sendParticles(ParticleTypes.LAVA, impact.x, impact.y, impact.z, 0, Math.cos(angle), 0.45, Math.sin(angle), burstSpeed * 0.8);
        }
    }
}

package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import com.mojang.math.Vector3f;
import iskallia.vault.init.ModParticles;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

/**
 * Server-sent particle cues for spell casting, visible to every player near the caster: the blue ring that circles a
 * charging spell, the blue flare when the charge fills, and the red flare of a Power Dump.
 */
public final class SpellParticles {
    private static final double RING_HEIGHT = 0.9D;
    private static final double RING_RADIUS_START = 0.6D;
    private static final double RING_RADIUS_FULL = 1.0D;
    private static final double RING_STEP_RADIANS = 0.45D;
    private static final int RING_POINTS = 2;
    private static final float RING_SCALE_START = 0.6F;
    private static final float RING_SCALE_FULL = 1.2F;
    private static final Vector3f RING_COLOR_START = new Vector3f(0.15F, 0.3F, 0.75F);
    private static final Vector3f RING_COLOR_FULL = new Vector3f(0.4F, 0.8F, 1.0F);

    private static final double FLARE_HEIGHT = 1.0D;
    private static final int CHARGE_FLARE_FLAMES = 16;
    private static final double CHARGE_FLARE_SPEED = 0.08D;
    private static final int CHARGE_FLARE_DUST = 10;
    private static final double CHARGE_FLARE_SPREAD = 0.35D;

    private static final int DUMP_FLARE_FLAMES = 30;
    private static final double DUMP_FLARE_SPEED = 0.12D;
    private static final int DUMP_FLARE_DUST = 16;
    private static final double DUMP_FLARE_SPREAD = 0.5D;
    private static final DustParticleOptions DUMP_DUST = new DustParticleOptions(new Vector3f(0.9F, 0.1F, 0.08F), 1.6F);

    private SpellParticles() {
    }

    /**
     * One tick of the charge ring around {@code player}: {@link #RING_POINTS} blue dust motes that advance around the
     * player each tick, widening and brightening as {@code progress} (0-1) fills.
     */
    public static void chargeRing(ServerPlayer player, int heldTicks, float progress) {
        float t = Mth.clamp(progress, 0.0F, 1.0F);
        double radius = Mth.lerp(t, RING_RADIUS_START, RING_RADIUS_FULL);
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                Mth.lerp(t, RING_COLOR_START.x(), RING_COLOR_FULL.x()),
                Mth.lerp(t, RING_COLOR_START.y(), RING_COLOR_FULL.y()),
                Mth.lerp(t, RING_COLOR_START.z(), RING_COLOR_FULL.z())),
                Mth.lerp(t, RING_SCALE_START, RING_SCALE_FULL));
        ServerLevel level = player.getLevel();
        double y = player.getY() + RING_HEIGHT;
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = heldTicks * RING_STEP_RADIANS + i * (Math.PI * 2.0D / RING_POINTS);
            level.sendParticles(dust, player.getX() + Math.cos(angle) * radius, y,
                    player.getZ() + Math.sin(angle) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void chargeFull(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        double y = player.getY() + FLARE_HEIGHT;
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), y, player.getZ(),
                CHARGE_FLARE_FLAMES, 0.0D, 0.0D, 0.0D, CHARGE_FLARE_SPEED);
        level.sendParticles(new DustParticleOptions(RING_COLOR_FULL, RING_SCALE_FULL), player.getX(), y, player.getZ(),
                CHARGE_FLARE_DUST, CHARGE_FLARE_SPREAD, CHARGE_FLARE_SPREAD, CHARGE_FLARE_SPREAD, 0.0D);
    }

    public static void powerDump(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        double y = player.getY() + FLARE_HEIGHT;
        level.sendParticles(ModParticles.RED_FLAME.get(), player.getX(), y, player.getZ(),
                DUMP_FLARE_FLAMES, 0.0D, 0.0D, 0.0D, DUMP_FLARE_SPEED);
        level.sendParticles(DUMP_DUST, player.getX(), y, player.getZ(),
                DUMP_FLARE_DUST, DUMP_FLARE_SPREAD, DUMP_FLARE_SPREAD, DUMP_FLARE_SPREAD, 0.0D);
    }
}

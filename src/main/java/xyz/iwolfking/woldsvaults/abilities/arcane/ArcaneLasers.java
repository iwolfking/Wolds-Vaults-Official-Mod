package xyz.iwolfking.woldsvaults.abilities.arcane;

import com.mojang.math.Vector3f;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.ArcaneBeamMessage;
import xyz.iwolfking.woldsvaults.network.message.ArcaneSparkleMessage;

import java.util.List;

/**
 * Arcane and Arcane Rail drawn as lasers instead of Vault Hunters' particle trails. Each shot is sent to nearby clients
 * as a laser from the caster's hand to where the ray stops, and handed to {@link BeamOptics} in case something bends it.
 */
public final class ArcaneLasers {
    private static final int ARCANE_RGB = 0xB45CFF;
    private static final int RAIL_RGB = 0xE070FF;
    private static final Vector3f RAIL_SPARKS = new Vector3f(0.9F, 0.55F, 1.0F);

    private ArcaneLasers() {
    }

    /** An Arcane beam just fired, after Vault Hunters' own ray: sends the laser and hands the ray to the bender. */
    public static void arcaneFired(ServerPlayer player, float range, float percentAbilityPower) {
        Vec3 start = player.getEyePosition().add(0.0, -WoldConstants.ARCANE_RAY_EYE_DROP, 0.0);
        Vec3 end = rayEnd(player, start, range, ClipContext.Block.COLLIDER);
        beam(player, List.of(ArcanePrisms.castingHand(player), end), ARCANE_RGB, 0.24F, 2);
        BeamOptics.bender().rayFired(player, start, end, percentAbilityPower, 3, 0.2F);
    }

    /**
     * An Arcane Rail bolt just fired: a thick laser along the bolt's line (out to 128 blocks or the first block) that
     * lingers and fades, handed to the bender for its average share of ability power.
     */
    public static void railFired(ServerPlayer player, float percentMin, float percentMax) {
        Vec3 start = player.getEyePosition().add(0.0, -WoldConstants.ARCANE_RAY_EYE_DROP, 0.0);
        Vec3 end = rayEnd(player, start, WoldConstants.ARCANE_RAIL_RANGE, ClipContext.Block.OUTLINE);
        beam(player, List.of(ArcanePrisms.castingHand(player), end), RAIL_RGB, 0.55F, 14);
        sparkle(player, end, RAIL_SPARKS, 24, 0.4);
        BeamOptics.bender().rayFired(player, start, end, (percentMin + percentMax) / 2.0F, 12, 0.35F);
    }

    private static Vec3 rayEnd(ServerPlayer player, Vec3 start, double range, ClipContext.Block shape) {
        Vec3 end = start.add(player.getLookAngle().scale(range));
        BlockHitResult wall = player.getLevel().clip(new ClipContext(start, end, shape, ClipContext.Fluid.NONE, player));
        return wall.getType() == HitResult.Type.MISS ? end : wall.getLocation();
    }

    /** Sends a laser through the points, kept for {@code ticks} ticks, to the caster and everyone tracking them. */
    public static void beam(ServerPlayer caster, List<Vec3> points, int rgb, float width, int ticks) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster), new ArcaneBeamMessage(points, rgb, width, ticks));
    }

    /** Sends a puff of glowing sparks in the color at {@code at} to everyone tracking {@code near} (and to it, if a player). */
    public static void sparkle(Entity near, Vec3 at, Vector3f color, int count, double spread) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> near),
                new ArcaneSparkleMessage(at, Mth.clamp(count, 1, 100), ColorUtil.rgb(color), (float) spread));
    }
}

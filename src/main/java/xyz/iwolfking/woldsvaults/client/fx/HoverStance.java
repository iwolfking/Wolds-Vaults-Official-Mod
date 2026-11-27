package xyz.iwolfking.woldsvaults.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client view of the post-dash Tidal Hover: which players are hovering, how far each has blended into the ready
 * stance, and the stance itself. While a player hovers in the air their fall distance is held at zero on this
 * client, which keeps ParCool's falling flail from starting; the server tracks fall damage separately.
 */
@OnlyIn(Dist.CLIENT)
public final class HoverStance {
    private static final float BLEND_STEP = 0.25F;
    private static final Map<Integer, State> STATES = new HashMap<>();

    private HoverStance() {
    }

    private static final class State {
        private long until;
        private float blend;
        private float previousBlend;
    }

    static void start(ClientLevel level, int entityId, int ticks) {
        State state = STATES.computeIfAbsent(entityId, id -> new State());
        state.until = level.getGameTime() + ticks;
    }

    static void stop(int entityId) {
        State state = STATES.get(entityId);
        if (state != null) {
            state.until = 0L;
        }
    }

    static void clear() {
        STATES.clear();
    }

    static boolean isActive(ClientLevel level, int entityId) {
        State state = STATES.get(entityId);
        return state != null && (state.until > level.getGameTime() || state.blend > 0.0F);
    }

    static void tick(ClientLevel level) {
        long now = level.getGameTime();
        Iterator<Map.Entry<Integer, State>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, State> entry = iterator.next();
            State state = entry.getValue();
            Entity entity = level.getEntity(entry.getKey());
            boolean hovering = entity instanceof Player player && state.until > now && isAirborne(player);
            if (hovering) {
                entity.fallDistance = 0.0F;
            }
            state.previousBlend = state.blend;
            state.blend = Mth.clamp(state.blend + (hovering ? BLEND_STEP : -BLEND_STEP), 0.0F, 1.0F);
            if (entity == null || (state.until <= now && state.blend <= 0.0F && state.previousBlend <= 0.0F)) {
                iterator.remove();
            }
        }
    }

    private static boolean isAirborne(Player player) {
        return !player.isOnGround() && !player.isShiftKeyDown() && !player.isInWater() && !player.isPassenger();
    }

    /** How far {@code entityId} is into the stance this frame, 0 when not hovering. */
    static float blend(int entityId, float partialTick) {
        State state = STATES.get(entityId);
        if (state == null) {
            return 0.0F;
        }
        return Mth.lerp(partialTick, state.previousBlend, state.blend);
    }

    /**
     * Poses {@code model} into a mid-air ready stance, blended over vanilla's pose: weapon arm raised forward, off
     * arm across as a guard, lead knee drawn up and trailing leg hanging back, with a slow breathing sway. Runs after
     * the model's own setup, so the caller copies the limbs onto the outer layers afterwards.
     */
    public static boolean apply(HumanoidModel<?> model, Player player, float ageInTicks) {
        if (STATES.isEmpty()) {
            return false;
        }
        float blend = blend(player.getId(), Minecraft.getInstance().getFrameTime());
        if (blend <= 0.0F) {
            return false;
        }
        float ease = blend * blend * (3.0F - 2.0F * blend);
        float sway = Mth.sin(ageInTicks * 0.12F);
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        ModelPart weaponArm = rightHanded ? model.rightArm : model.leftArm;
        ModelPart guardArm = rightHanded ? model.leftArm : model.rightArm;
        ModelPart leadLeg = rightHanded ? model.rightLeg : model.leftLeg;
        ModelPart trailLeg = rightHanded ? model.leftLeg : model.rightLeg;
        float mirror = rightHanded ? 1.0F : -1.0F;
        pose(weaponArm, -1.15F + sway * 0.04F, -0.15F * mirror, 0.12F * mirror, ease);
        pose(guardArm, -0.5F + sway * 0.03F, 0.55F * mirror, -0.3F * mirror, ease);
        pose(leadLeg, -0.75F + sway * 0.05F, 0.1F * mirror, 0.06F * mirror, ease);
        pose(trailLeg, 0.3F - sway * 0.04F, -0.05F * mirror, -0.08F * mirror, ease);
        return true;
    }

    private static void pose(ModelPart part, float x, float y, float z, float ease) {
        part.xRot = Mth.lerp(ease, part.xRot, x);
        part.yRot = Mth.lerp(ease, part.yRot, y);
        part.zRot = Mth.lerp(ease, part.zRot, z);
    }
}

package xyz.iwolfking.woldsvaults.client.renderers.station;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The block-entity-rendered half of a station. Offsets and pivots are machine pixels relative to the
 * controller block's minimum corner in the facing=north frame; the renderer rotates the whole
 * group with the block's facing.
 */
public final class ExtractorStationRig {
    public record Part(String name, ResourceLocation model, float ox, float oy, float oz, boolean translucent, boolean fullbright) {
    }

    public record Bone(String name, @Nullable String parent, float px, float py, float pz) {
    }

    /** One bone's pose at phase t: rotations in degrees, translation in machine pixels. */
    public record Pose(float rx, float ry, float rz, float tx, float ty, float tz) {
        public static final Pose REST = new Pose(0, 0, 0, 0, 0, 0);
    }

    /**
     * A rig's working cycle. {@code t} is the cycle phase in [0, 1) and {@code k} the working intensity: 0 gives the
     * idle pose, 1 the full cycle, and values between ease from one to the other. Only spins may depend on
     * {@code t} at {@code k} = 0; the phase stops advancing while the rig is idle, so they hold still.
     */
    public interface PoseFunction {
        Pose pose(String bone, float t, float k);
    }

    public final String id;
    public final int cycleTicks;
    public final int blendTicks;
    public final List<Part> parts = new ArrayList<>();
    public final Map<String, Bone> bones = new LinkedHashMap<>();
    public final PoseFunction poser;
    public final boolean showsExtractorPreview;

    private ExtractorStationRig(String id, int cycleTicks, int blendTicks, PoseFunction poser, boolean showsExtractorPreview) {
        this.id = id;
        this.cycleTicks = cycleTicks;
        this.blendTicks = blendTicks;
        this.poser = poser;
        this.showsExtractorPreview = showsExtractorPreview;
    }

    private ExtractorStationRig part(String name, float ox, float oy, float oz, boolean translucent, boolean fullbright) {
        this.parts.add(new Part(name, WoldsVaults.id("block/" + this.id + "/ber_" + name), ox, oy, oz, translucent, fullbright));
        return this;
    }

    private ExtractorStationRig bone(String name, @Nullable String parent, float px, float py, float pz) {
        this.bones.put(name, new Bone(name, parent, px, py, pz));
        return this;
    }

    private static double sin(double x) {
        return Math.sin(x);
    }

    private static double cos(double x) {
        return Math.cos(x);
    }

    public static final ExtractorStationRig ASSEMBLER = new ExtractorStationRig("extractor_assembler", 120, 10, (bone, t, k) -> {
        double w = 2.0D * Math.PI * t;
        return switch (bone) {
            case "head" -> new Pose(0, 0, 0, 0, (float) (-3.0D * k * (0.5D - 0.5D * cos(w))), 0);
            default -> Pose.REST;
        };
    }, true)
            .part("head", 0, 33, 0, false, true)
            .part("vitrine_glass", -3, 15, -3, true, false)
            .bone("head", null, 8, 41, 8);

    public static final ExtractorStationRig FORGE = new ExtractorStationRig("extraction_forge", 80, 10, (bone, t, k) -> {
        double w = 2.0D * Math.PI * t;
        double weld = k * (0.5D - 0.5D * cos(w));
        double grip = k * (0.5D + 0.5D * cos(w));
        return switch (bone) {
            case "arm_l_upper" -> new Pose(0, 0, (float) (3.0D * weld), 0, 0, 0);
            case "arm_l_fore" -> new Pose(0, 0, (float) (-3.0D * weld), 0, 0, 0);
            case "arm_l_head" -> new Pose(0, 0, 0, 0, (float) (5.4D * weld), (float) (-1.2D + 6.0D * Math.pow(weld, 4)));
            case "arm_r_upper" -> new Pose(0, 0, (float) (-3.0D * grip), 0, 0, 0);
            case "arm_r_fore" -> new Pose(0, 0, (float) (3.0D * grip), 0, 0, 0);
            case "arm_r_head" -> new Pose(0, (float) (30.0D * grip * grip), 0, 0, (float) (3.0D + 3.8D * grip), (float) (-5.0D * Math.pow(grip, 5)));
            case "workpiece" -> new Pose(0, 360.0F * t, 0, 0, (float) (0.6D * k * sin(2.0D * w)), 0);
            case "platter" -> new Pose(0, 360.0F * t, 0, 0, 0, 0);
            default -> Pose.REST;
        };
    }, false)
            .part("arm_l_base", -12, 42, -26, false, false)
            .part("arm_l_upper", -6, 44, -24, false, false)
            .part("arm_l_fore", 6, 40, -24, false, false)
            .part("arm_l_head", 5, 34, -25, false, true)
            .part("arm_r_base", 22, 42, -10, false, false)
            .part("arm_r_upper", 10, 44, -8, false, false)
            .part("arm_r_fore", 6, 40, -8, false, false)
            .part("arm_r_head", 5, 33, -9, false, false)
            .part("platter", 0, 28, -22, false, true)
            .part("workpiece", 4, 32, -18, false, true)
            .part("uv_bar", -6, 49, -24, false, true)
            .part("window", -4, 28, -28, true, false)
            .bone("arm_l_base", null, -9, 46, -22)
            .bone("arm_l_upper", "arm_l_base", -6, 46, -22)
            .bone("arm_l_fore", "arm_l_upper", 8, 47, -22)
            .bone("arm_l_head", "arm_l_fore", 8, 41, -22)
            .bone("arm_r_base", null, 25, 46, -6)
            .bone("arm_r_upper", "arm_r_base", 22, 46, -6)
            .bone("arm_r_fore", "arm_r_upper", 8, 47, -6)
            .bone("arm_r_head", "arm_r_fore", 8, 41, -6)
            .bone("workpiece", null, 8, 35, -14)
            .bone("platter", null, 8, 30, -14);

    public static List<ExtractorStationRig> all() {
        return List.of(ASSEMBLER, FORGE);
    }
}

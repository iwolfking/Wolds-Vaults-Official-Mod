package xyz.iwolfking.woldsvaults.client.renderers.station;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The block-entity-rendered half of a station, transcribed from the art manifests
 * ({@code art/extractor_stations/out/<station>_manifest.json}) and the animation tables in
 * SPEC_EXTRACTOR_STATION_ART.md §5. Offsets and pivots are machine pixels relative to the
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

    public interface PoseFunction {
        Pose pose(String bone, float t);
    }

    public final String id;
    public final int cycleTicks;
    public final List<Part> parts = new ArrayList<>();
    public final Map<String, Bone> bones = new LinkedHashMap<>();
    public final PoseFunction poser;
    public final boolean showsExtractorPreview;

    private ExtractorStationRig(String id, int cycleTicks, PoseFunction poser, boolean showsExtractorPreview) {
        this.id = id;
        this.cycleTicks = cycleTicks;
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

    public static final ExtractorStationRig ASSEMBLER = new ExtractorStationRig("extractor_assembler", 120, (bone, t) -> {
        double w = 2.0D * Math.PI * t;
        return switch (bone) {
            case "head" -> new Pose(0, 0, 0, 0, (float) (-3.0D * (0.5D - 0.5D * cos(w))), 0);
            default -> Pose.REST;
        };
    }, true)
            .part("head", 0, 33, 0, false, true)
            .part("vitrine_glass", -3, 15, -3, true, false)
            .bone("head", null, 8, 41, 8);

    public static final ExtractorStationRig FORGE = new ExtractorStationRig("extraction_forge", 80, (bone, t) -> {
        double w = 2.0D * Math.PI * t;
        return switch (bone) {
            case "arm_l_base" -> new Pose(0, (float) (6.0D * sin(w / 2.0D)), 0, 0, 0, 0);
            case "arm_l_upper" -> new Pose(0, 0, (float) (-12.0D + 8.0D * sin(w)), 0, 0, 0);
            case "arm_l_fore" -> new Pose(0, 0, (float) (24.0D - 12.0D * sin(w)), 0, 0, 0);
            case "arm_l_head" -> new Pose(0, 0, (float) (-8.0D * sin(w)), 0, 0, (float) (4.0D * cos(w)));
            case "arm_r_base" -> new Pose(0, (float) (-5.0D * sin(w / 2.0D)), 0, 0, 0, 0);
            case "arm_r_upper" -> new Pose(0, 0, (float) (10.0D + 6.0D * sin(w + 2.1D)), 0, 0, 0);
            case "arm_r_fore" -> new Pose(0, 0, (float) (-18.0D - 8.0D * sin(w + 2.1D)), 0, 0, 0);
            case "arm_r_head" -> new Pose(0, (float) (30.0D * sin(w)), 0, 0, 0, 0);
            case "workpiece" -> new Pose(0, 360.0F * t, 0, 0, (float) (0.6D * sin(2.0D * w)), 0);
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

package xyz.iwolfking.woldsvaults.entities.extraction.client;

import java.util.regex.Pattern;

/**
 * Procedural motion for the extractor rig, keyed on bone-name suffixes.
 *
 * <p>Animation files address bones by name, and the rule that keeps 33 part variants
 * affordable is that the shipped animations only ever touch slot bones and the fixed
 * drive. If every scanner and radiator needed its own track, each new part would become
 * animation work. So a part opts into motion purely by NAMING a bone with a suffix, and
 * this class applies the transform after the controllers have run.
 *
 * <p>These constants are mirrored exactly in the art generator's {@code tools/motion.py},
 * which is what the review renders are produced from. Change one, change the other.
 */
public final class ExtractorMotion {
    /** A chase bone is "<part>_chase<i>of<n>": one lit segment of an n-segment loop. */
    public static final Pattern CHASE = Pattern.compile("_chase(\\d+)of(\\d+)$");
    public static final String CHASE_KEY = "_chase";

    public static final String SPIN = "_spin";
    public static final String SPIN_X = "_spinx";
    public static final String SPIN_Z = "_spinz";
    public static final String SHAKE = "_shake";
    public static final String PULSE = "_pulse";
    public static final String BOB = "_bob";
    public static final String PUMP = "_pump";

    private static final float SPIN_IDLE_REV_S = 0.10F;
    private static final float SPIN_DRILL_REV_S = 0.55F;

    private static final float SHAKE_UNITS = 0.14F;
    private static final float SHAKE_FX = 13.1F;
    private static final float SHAKE_FY = 17.7F;
    private static final float SHAKE_FZ = 11.3F;

    private static final float PULSE_SCALE = 0.08F;
    private static final float PULSE_PERIOD_IDLE = 1.6F;
    private static final float PULSE_PERIOD_DRILL = 0.55F;

    private static final float BOB_UNITS = 0.4F;
    private static final float BOB_PERIOD = 4.0F;

    // a nodding derrick: a real stroke, not a vibration, and only while working
    private static final float PUMP_UNITS = 1.0F;
    private static final float PUMP_PERIOD = 0.7F;

    // a lit segment travelling round a loop. Brightness is a per-SHEET knob, so a
    // segment is lit by being drawn at all: outside its turn it is scaled to zero.
    private static final float CHASE_LAP_IDLE = 3.2F;
    private static final float CHASE_LAP_DRILL = 1.1F;
    private static final int CHASE_WINDOW = 3;

    private static final float PULSE_BRIGHT_IDLE = 0.10F;
    private static final float PULSE_BRIGHT_BASE = 0.55F;
    private static final float PULSE_BRIGHT_SWING = 0.45F;
    private static final float PULSE_BRIGHT_PERIOD = 0.9F;

    private ExtractorMotion() {
    }

    /**
     * Degrees of accumulated rotation for a spin bone. The SPEED is blended between idle
     * and drilling rather than the angle, so a dish winds up and coasts down instead of
     * snapping when the rig starts or stops.
     */
    public static float spinDegrees(float seconds, float ramp) {
        float revPerSecond = SPIN_IDLE_REV_S + (SPIN_DRILL_REV_S - SPIN_IDLE_REV_S) * ramp;
        return (seconds * revPerSecond * 360.0F) % 360.0F;
    }

    public static float shakeX(float seconds, float ramp) {
        return (float) Math.sin(seconds * SHAKE_FX) * SHAKE_UNITS * ramp;
    }

    public static float shakeY(float seconds, float ramp) {
        return (float) Math.sin(seconds * SHAKE_FY) * SHAKE_UNITS * 0.6F * ramp;
    }

    public static float shakeZ(float seconds, float ramp) {
        return (float) Math.sin(seconds * SHAKE_FZ) * SHAKE_UNITS * ramp;
    }

    public static float pulseScale(float seconds, float ramp) {
        float period = PULSE_PERIOD_IDLE + (PULSE_PERIOD_DRILL - PULSE_PERIOD_IDLE) * ramp;
        return 1.0F + PULSE_SCALE * 0.5F
                * (1.0F + (float) Math.sin(seconds * 2.0F * Math.PI / period));
    }

    public static float pumpY(float seconds, float ramp) {
        return (float) Math.sin(seconds * 2.0F * Math.PI / PUMP_PERIOD) * PUMP_UNITS * ramp;
    }

    /**
     * 1 while this segment is inside the travelling window, else 0. The head of the
     * window is full size and the tail tapers, so the streak has direction.
     */
    public static float chaseScale(float seconds, float ramp, int index, int count) {
        float lap = CHASE_LAP_IDLE + (CHASE_LAP_DRILL - CHASE_LAP_IDLE) * ramp;
        float head = (seconds / lap) * count;
        float back = ((head - index) % count + count) % count;
        if (back >= CHASE_WINDOW) {
            return 0.0F;
        }
        return 1.0F - 0.55F * (back / CHASE_WINDOW);
    }

    public static float bobY(float seconds) {
        return (float) Math.sin(seconds * 2.0F * Math.PI / BOB_PERIOD) * BOB_UNITS;
    }

    /**
     * Colour multiplier for the pulsing emissive layer. {@code RenderType.eyes} adds
     * light rather than replacing it, so scaling rgb scales how much light is added --
     * which is how a per-layer brightness knob is possible at all.
     */
    public static float pulseBrightness(float seconds, float ramp) {
        float lit = PULSE_BRIGHT_BASE + PULSE_BRIGHT_SWING
                * (float) Math.sin(seconds * 2.0F * Math.PI / PULSE_BRIGHT_PERIOD);
        return PULSE_BRIGHT_IDLE + (lit - PULSE_BRIGHT_IDLE) * ramp;
    }

    /** The motion suffix a bone carries, or null if it is not an animated part. */
    public static String suffixOf(String boneName) {
        if (CHASE.matcher(boneName).find()) {
            return CHASE_KEY;
        }
        if (boneName.endsWith(SPIN_X)) {
            return SPIN_X;
        }
        if (boneName.endsWith(SPIN_Z)) {
            return SPIN_Z;
        }
        if (boneName.endsWith(SPIN)) {
            return SPIN;
        }
        if (boneName.endsWith(SHAKE)) {
            return SHAKE;
        }
        if (boneName.endsWith(PULSE)) {
            return PULSE;
        }
        if (boneName.endsWith(BOB)) {
            return BOB;
        }
        if (boneName.endsWith(PUMP)) {
            return PUMP;
        }
        return null;
    }
}

package xyz.iwolfking.woldsvaults.client.renderers.station;

/**
 * The client-side motion of one station: the cycle phase, which advances only while the rig moves, and the working
 * intensity, which eases between 0 (idle pose) and 1 (full cycle) over the rig's blend time. Both follow the level's
 * game time plus the partial tick, so they move smoothly between ticks and stop while the game is paused.
 */
public final class ExtractorStationMotion {
    private double phase;
    private double ramp;
    private double lastTime = Double.NaN;

    /** Advances the motion to {@code now}, in game ticks with the partial tick, easing toward {@code working}. */
    public void advance(double now, boolean working, ExtractorStationRig rig) {
        double target = working ? 1.0D : 0.0D;
        if (Double.isNaN(this.lastTime)) {
            this.lastTime = now;
            this.ramp = target;
            return;
        }
        double elapsed = Math.max(0.0D, now - this.lastTime);
        this.lastTime = now;
        double speedBefore = ease(this.ramp);
        double step = elapsed / rig.blendTicks;
        this.ramp = working ? Math.min(1.0D, this.ramp + step) : Math.max(0.0D, this.ramp - step);
        double speed = 0.5D * (speedBefore + ease(this.ramp));
        this.phase = (this.phase + speed * elapsed / rig.cycleTicks) % 1.0D;
    }

    /** The cycle phase in [0, 1). */
    public float phase() {
        return (float) this.phase;
    }

    /** The eased working intensity in [0, 1]. */
    public float intensity() {
        return (float) ease(this.ramp);
    }

    private static double ease(double x) {
        return x * x * (3.0D - 2.0D * x);
    }
}

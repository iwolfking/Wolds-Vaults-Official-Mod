package xyz.iwolfking.woldsvaults.loadingzone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// A loading zone's outline, from its corner posts: two posts make a gate to walk through, three or more an area to stand in.
// It also says where the zone leads: an arrival marker in the same room, a vault pocket, or back out of one.
public record LoadingZoneShape(String link, double[] xs, double[] zs, double minY, double maxY, Leads leads, String pocket) {
    public record Corner(double x, double y, double z, int height, int order) {
    }

    public enum Leads {
        ARRIVAL("Arrival marker"),
        POCKET("Vault pocket"),
        RETURN("Back out of pocket");

        private final String label;

        Leads(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }

        public Leads next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Leads byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : ARRIVAL;
        }
    }

    // numbered corners keep their numbers' order, so concave areas work; otherwise they go round their middle, which suits any convex area
    public static LoadingZoneShape of(String link, List<Corner> corners, Leads leads, String pocket) {
        double centreX = corners.stream().mapToDouble(Corner::x).average().orElse(0.0);
        double centreZ = corners.stream().mapToDouble(Corner::z).average().orElse(0.0);
        List<Corner> sorted = new ArrayList<>(corners);
        if (sorted.stream().allMatch(corner -> corner.order() > 0)) {
            sorted.sort(Comparator.comparingInt(Corner::order));
        } else {
            sorted.sort(Comparator.comparingDouble(corner -> Math.atan2(corner.z() - centreZ, corner.x() - centreX)));
        }
        double[] xs = new double[sorted.size()];
        double[] zs = new double[sorted.size()];
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int i = 0; i < sorted.size(); i++) {
            Corner corner = sorted.get(i);
            xs[i] = corner.x();
            zs[i] = corner.z();
            minY = Math.min(minY, corner.y());
            maxY = Math.max(maxY, corner.y() + corner.height());
        }
        return new LoadingZoneShape(link, xs, zs, minY, maxY, leads, pocket);
    }

    public boolean isGate() {
        return this.xs.length == 2;
    }

    public double centreX() {
        double total = 0.0;
        for (double x : this.xs) {
            total += x;
        }
        return total / this.xs.length;
    }

    public double centreZ() {
        double total = 0.0;
        for (double z : this.zs) {
            total += z;
        }
        return total / this.zs.length;
    }

    public boolean spans(double y) {
        return y >= this.minY && y < this.maxY;
    }

    // even-odd rule: a point is inside when a ray from it crosses the outline an odd number of times
    public boolean contains(double x, double y, double z) {
        if (isGate() || !spans(y)) {
            return false;
        }
        boolean inside = false;
        for (int i = 0, j = this.xs.length - 1; i < this.xs.length; j = i++) {
            if ((this.zs[i] > z) != (this.zs[j] > z) && x < (this.xs[j] - this.xs[i]) * (z - this.zs[i]) / (this.zs[j] - this.zs[i]) + this.xs[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    // tested on the path since last tick, so nothing moving fast can step over the line between two ticks
    public boolean crossedBy(double fromX, double fromZ, double toX, double toZ, double y) {
        if (!isGate() || !spans(y)) {
            return false;
        }
        boolean wasAhead = side(this.xs[0], this.zs[0], this.xs[1], this.zs[1], fromX, fromZ) >= 0.0;
        boolean isAhead = side(this.xs[0], this.zs[0], this.xs[1], this.zs[1], toX, toZ) >= 0.0;
        double first = side(fromX, fromZ, toX, toZ, this.xs[0], this.zs[0]);
        double second = side(fromX, fromZ, toX, toZ, this.xs[1], this.zs[1]);
        return wasAhead != isAhead && first * second <= 0.0;
    }

    private static double side(double ax, double az, double bx, double bz, double px, double pz) {
        return (bx - ax) * (pz - az) - (bz - az) * (px - ax);
    }
}

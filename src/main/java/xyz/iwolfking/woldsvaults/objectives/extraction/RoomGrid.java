package xyz.iwolfking.woldsvaults.objectives.extraction;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * A snapshot of one extraction site room's walkable space, read from the world a few block planes per tick, and the
 * baked lane data derived from it: the lane cells wave mobs can reach from the caves, a distance field towards every
 * pad (one for ordinary mobs and one restricted to cells wide enough for 2-wide mobs), each cave's home pad, and the
 * cells where cache chests may stand. Cells are feet positions: a solid block below and two passable blocks above.
 * Mob moves follow the room checker's model: four neighbours, up at most 1, down at most 3.
 */
public final class RoomGrid {
    private static final int MOB_UP = 1;
    private static final int MOB_DOWN = 3;
    private static final int PLAYER_UP = 3;
    private static final int PLAYER_DOWN = 64;
    private static final int PLANES_PER_TICK = 2;
    private static final int CAVE_SEED_RADIUS = 3;
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final int siteIndex;
    private final BlockPos origin;
    private final int sx;
    private final int sy;
    private final int sz;
    private final List<Vec3> pads;
    private final List<BlockPos> caves;
    private final BitSet passable;
    private int nextPlane;
    private boolean ready;
    private boolean failed;

    private int[][] columns;
    private final IntOpenHashSet lane = new IntOpenHashSet();
    private final IntOpenHashSet wide = new IntOpenHashSet();
    private final List<IntArrayList> caveSeeds = new ArrayList<>();
    private final List<Int2IntOpenHashMap> fields = new ArrayList<>();
    private final List<Int2IntOpenHashMap> wideFields = new ArrayList<>();
    private int[] caveCells = new int[0];
    private int[] caveWideCells = new int[0];
    private int[][] caveHomes = new int[0][];
    private int[] chestCandidates = new int[0];
    private final IntOpenHashSet badCaves = new IntOpenHashSet();

    public RoomGrid(int siteIndex, BlockPos origin, int sizeX, int sizeY, int sizeZ, List<Vec3> pads, List<BlockPos> caves) {
        this.siteIndex = siteIndex;
        this.origin = origin;
        this.sx = sizeX;
        this.sy = sizeY;
        this.sz = sizeZ;
        this.pads = pads;
        this.caves = caves;
        this.passable = new BitSet(sizeX * sizeY * sizeZ);
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean hasFailed() {
        return this.failed;
    }

    public int padCount() {
        return this.pads.size();
    }

    public int caveCount() {
        return this.caves.size();
    }

    public BlockPos caveAnchor(int cave) {
        return this.caves.get(cave);
    }

    /** Reads the next few block planes; returns true once the whole room is read and the lane data is built. */
    public boolean tickBuild(Level level, int chestMobMargin, int chestPadClearance) {
        if (this.ready || this.failed) {
            return this.ready;
        }
        int end = Math.min(this.sx, this.nextPlane + PLANES_PER_TICK);
        this.readPlanes(level, this.nextPlane, end);
        this.nextPlane = end;
        if (this.nextPlane >= this.sx) {
            this.derive(chestMobMargin, chestPadClearance);
        }
        return this.ready;
    }

    /** Reads every remaining plane in one go, for a fight that resumes after a restart. */
    public void buildNow(Level level, int chestMobMargin, int chestPadClearance) {
        long start = System.nanoTime();
        while (!this.ready && !this.failed) {
            this.tickBuild(level, chestMobMargin, chestPadClearance);
        }
        WoldLogger.info("Extraction site {} room grid built in one tick ({} ms).", this.siteIndex, (System.nanoTime() - start) / 1_000_000L);
    }

    private void readPlanes(Level level, int fromX, int toX) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = fromX; x < toX; x++) {
            for (int y = 0; y < this.sy; y++) {
                for (int z = 0; z < this.sz; z++) {
                    pos.set(this.origin.getX() + x, this.origin.getY() + y, this.origin.getZ() + z);
                    if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                        this.passable.set(this.idx(x, y, z));
                    }
                }
            }
        }
    }

    private void derive(int chestMobMargin, int chestPadClearance) {
        long start = System.nanoTime();
        this.columns = new int[this.sx * this.sz][];
        for (int x = 0; x < this.sx; x++) {
            for (int z = 0; z < this.sz; z++) {
                IntArrayList ys = new IntArrayList();
                for (int y = 1; y < this.sy - 1; y++) {
                    if (!this.pass(x, y - 1, z) && this.pass(x, y, z) && this.pass(x, y + 1, z)) {
                        ys.add(y);
                    }
                }
                this.columns[x * this.sz + z] = ys.toIntArray();
            }
        }
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        for (BlockPos anchor : this.caves) {
            IntArrayList seeds = new IntArrayList();
            int ax = anchor.getX() - this.origin.getX();
            int ay = anchor.getY() - this.origin.getY();
            int az = anchor.getZ() - this.origin.getZ();
            for (int dx = -CAVE_SEED_RADIUS; dx <= CAVE_SEED_RADIUS; dx++) {
                for (int dz = -CAVE_SEED_RADIUS; dz <= CAVE_SEED_RADIUS; dz++) {
                    int x = ax + dx;
                    int z = az + dz;
                    if (!this.inXZ(x, z)) {
                        continue;
                    }
                    for (int y : this.columns[x * this.sz + z]) {
                        if (y >= ay - 4 && y <= ay + 2) {
                            seeds.add(this.idx(x, y, z));
                        }
                    }
                }
            }
            this.caveSeeds.add(seeds);
            for (int seed : seeds) {
                if (this.lane.add(seed)) {
                    queue.enqueue(seed);
                }
            }
        }
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            this.forEachMove(cell, MOB_UP, MOB_DOWN, null, next -> {
                if (this.lane.add(next)) {
                    queue.enqueue(next);
                }
            });
        }
        for (int cell : this.lane) {
            if (this.isWide(cell)) {
                this.wide.add(cell);
            }
        }
        for (int pad = 0; pad < this.pads.size(); pad++) {
            this.fields.add(this.field(pad, false));
            this.wideFields.add(this.field(pad, true));
        }
        this.caveCells = new int[this.caves.size()];
        this.caveWideCells = new int[this.caves.size()];
        this.caveHomes = new int[this.caves.size()][];
        for (int cave = 0; cave < this.caves.size(); cave++) {
            BlockPos anchor = this.caves.get(cave);
            this.caveCells[cave] = this.nearestIn(this.lane, anchor.getX() - this.origin.getX(), anchor.getY() - 1 - this.origin.getY(), anchor.getZ() - this.origin.getZ(), 4);
            this.caveWideCells[cave] = this.nearestIn(this.wide, anchor.getX() - this.origin.getX(), anchor.getY() - 1 - this.origin.getY(), anchor.getZ() - this.origin.getZ(), 6);
            int best = Integer.MAX_VALUE;
            IntArrayList homes = new IntArrayList();
            for (int pad = 0; pad < this.pads.size(); pad++) {
                int narrow = this.caveCells[cave] < 0 ? -1 : this.fields.get(pad).getOrDefault(this.caveCells[cave], -1);
                int broad = this.caveWideCells[cave] < 0 ? -1 : this.wideFields.get(pad).getOrDefault(this.caveWideCells[cave], -1);
                if (narrow < 0 || broad < 0) {
                    WoldLogger.error("Extraction site {} room grid: cave {} at {} cannot reach pad {} ({} field); that cave is dropped from the spawn rotation.",
                            this.siteIndex, cave, anchor, pad, narrow < 0 ? "narrow" : "2-wide");
                    this.badCaves.add(cave);
                    continue;
                }
                if (narrow < best - 1) {
                    best = narrow;
                    homes.clear();
                    homes.add(pad);
                } else if (Math.abs(narrow - best) <= 1) {
                    homes.add(pad);
                    best = Math.min(best, narrow);
                }
            }
            this.caveHomes[cave] = homes.toIntArray();
        }
        this.chestCandidates = this.chests(chestMobMargin, chestPadClearance);
        this.ready = true;
        int widest = 0;
        for (Int2IntOpenHashMap field : this.fields) {
            for (int value : field.values()) {
                widest = Math.max(widest, value);
            }
        }
        WoldLogger.info("Extraction site {} room grid ready: {} lane cells, {} 2-wide, largest lane distance {}, {} cache chest spots, {} of {} caves usable ({} ms).",
                this.siteIndex, this.lane.size(), this.wide.size(), widest, this.chestCandidates.length,
                this.caves.size() - this.badCaves.size(), this.caves.size(), (System.nanoTime() - start) / 1_000_000L);
        if (this.lane.isEmpty()) {
            WoldLogger.error("Extraction site {} room grid found no lane cells at all; wave mobs there fall back to vanilla pathing.", this.siteIndex);
        }
    }

    private Int2IntOpenHashMap field(int pad, boolean wideOnly) {
        IntOpenHashSet allowed = wideOnly ? this.wide : this.lane;
        Vec3 centre = this.pads.get(pad);
        int bx = (int) Math.floor(centre.x) - this.origin.getX();
        int bz = (int) Math.floor(centre.z) - this.origin.getZ();
        int feet = (int) Math.round(centre.y) - 2 - this.origin.getY();
        int inner = wideOnly ? 3 : 2;
        int outer = wideOnly ? 4 : 3;
        Int2IntOpenHashMap distance = new Int2IntOpenHashMap();
        distance.defaultReturnValue(-1);
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int cheb = Math.max(Math.abs(dx), Math.abs(dz));
                if (cheb < inner) {
                    continue;
                }
                int x = bx + dx;
                int z = bz + dz;
                if (!this.inXZ(x, z)) {
                    continue;
                }
                for (int y : this.columns[x * this.sz + z]) {
                    int cell = this.idx(x, y, z);
                    if (Math.abs(y - feet) <= 1 && allowed.contains(cell) && !distance.containsKey(cell)) {
                        distance.put(cell, 0);
                        queue.enqueue(cell);
                    }
                }
            }
        }
        if (distance.isEmpty()) {
            WoldLogger.error("Extraction site {} room grid: pad {} at {} has no {} lane cells around it; mobs cannot path to it.",
                    this.siteIndex, pad, centre, wideOnly ? "2-wide" : "narrow");
        }
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            int d = distance.get(cell);
            this.forEachPredecessor(cell, allowed, prev -> {
                if (!distance.containsKey(prev)) {
                    distance.put(prev, d + 1);
                    queue.enqueue(prev);
                }
            });
        }
        return distance;
    }

    private int[] chests(int mobMargin, int padClearance) {
        boolean[] mobColumn = new boolean[this.sx * this.sz];
        for (int cell : this.lane) {
            mobColumn[this.cx(cell) * this.sz + this.cz(cell)] = true;
        }
        boolean[] nearMob = new boolean[this.sx * this.sz];
        for (int x = 0; x < this.sx; x++) {
            for (int z = 0; z < this.sz; z++) {
                if (!mobColumn[x * this.sz + z]) {
                    continue;
                }
                for (int dx = -mobMargin; dx <= mobMargin; dx++) {
                    for (int dz = -mobMargin; dz <= mobMargin; dz++) {
                        if (this.inXZ(x + dx, z + dz)) {
                            nearMob[(x + dx) * this.sz + z + dz] = true;
                        }
                    }
                }
            }
        }
        IntOpenHashSet reach = new IntOpenHashSet(this.lane);
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        for (int cell : this.lane) {
            queue.enqueue(cell);
        }
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            this.forEachMove(cell, PLAYER_UP, PLAYER_DOWN, null, next -> {
                if (reach.add(next)) {
                    queue.enqueue(next);
                }
            });
        }
        IntArrayList out = new IntArrayList();
        for (int cell : reach) {
            int x = this.cx(cell);
            int y = this.cy(cell);
            int z = this.cz(cell);
            if (nearMob[x * this.sz + z] || Math.min(Math.min(x, this.sx - 1 - x), Math.min(z, this.sz - 1 - z)) < padClearance) {
                continue;
            }
            if (y + 2 >= this.sy || !this.pass(x, y + 2, z)) {
                continue;
            }
            boolean nearPad = false;
            for (Vec3 pad : this.pads) {
                int px = (int) Math.floor(pad.x) - this.origin.getX();
                int pz = (int) Math.floor(pad.z) - this.origin.getZ();
                if (Math.max(Math.abs(x - px), Math.abs(z - pz)) <= padClearance) {
                    nearPad = true;
                    break;
                }
            }
            if (!nearPad) {
                out.add(cell);
            }
        }
        return out.toIntArray();
    }

    private boolean isWide(int cell) {
        int x = this.cx(cell);
        int y = this.cy(cell);
        int z = this.cz(cell);
        if (y + 2 >= this.sy || !this.pass(x, y + 2, z)) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!this.inXZ(x + dx, z + dz)) {
                    return false;
                }
                boolean found = false;
                for (int ny : this.columns[(x + dx) * this.sz + z + dz]) {
                    if (Math.abs(ny - y) <= 1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    private interface CellSink {
        void accept(int cell);
    }

    private void forEachMove(int cell, int up, int down, IntOpenHashSet allowed, CellSink sink) {
        int x = this.cx(cell);
        int y = this.cy(cell);
        int z = this.cz(cell);
        for (int[] d : DIRS) {
            int nx = x + d[0];
            int nz = z + d[1];
            if (!this.inXZ(nx, nz)) {
                continue;
            }
            for (int ny : this.columns[nx * this.sz + nz]) {
                int dy = ny - y;
                if (dy > up || -dy > down) {
                    continue;
                }
                int next = this.idx(nx, ny, nz);
                if (allowed != null && !allowed.contains(next)) {
                    continue;
                }
                if (this.canStep(x, y, z, nx, ny, nz)) {
                    sink.accept(next);
                }
            }
        }
    }

    private void forEachPredecessor(int cell, IntOpenHashSet allowed, CellSink sink) {
        int x = this.cx(cell);
        int y = this.cy(cell);
        int z = this.cz(cell);
        for (int[] d : DIRS) {
            int px = x + d[0];
            int pz = z + d[1];
            if (!this.inXZ(px, pz)) {
                continue;
            }
            for (int py : this.columns[px * this.sz + pz]) {
                int dy = y - py;
                if (dy > MOB_UP || -dy > MOB_DOWN) {
                    continue;
                }
                int prev = this.idx(px, py, pz);
                if (allowed.contains(prev) && this.canStep(px, py, pz, x, y, z)) {
                    sink.accept(prev);
                }
            }
        }
    }

    private boolean canStep(int x, int y, int z, int nx, int ny, int nz) {
        int dy = ny - y;
        if (dy > 0) {
            if (y + dy + 2 > this.sy) {
                return false;
            }
            for (int yy = y; yy < y + dy + 2; yy++) {
                if (!this.pass(x, yy, z)) {
                    return false;
                }
            }
        } else if (dy < 0) {
            for (int yy = ny; yy < y + 2; yy++) {
                if (!this.pass(nx, yy, nz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int nearestIn(IntOpenHashSet set, int x, int y, int z, int radius) {
        int best = -1;
        int bestScore = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (!this.inXZ(x + dx, z + dz)) {
                    continue;
                }
                for (int ny : this.columns[(x + dx) * this.sz + z + dz]) {
                    int dy = Math.abs(ny - y);
                    if (dy > 3) {
                        continue;
                    }
                    int cell = this.idx(x + dx, ny, z + dz);
                    int score = Math.abs(dx) + Math.abs(dz) + 2 * dy;
                    if (score < bestScore && set.contains(cell)) {
                        bestScore = score;
                        best = cell;
                    }
                }
            }
        }
        return best;
    }

    /** The lane cell a mob standing at {@code pos} occupies, or the nearest one within 3 blocks; -1 when none is near. */
    public int cellAt(BlockPos pos, boolean wideOnly) {
        if (!this.ready) {
            return -1;
        }
        int x = pos.getX() - this.origin.getX();
        int y = pos.getY() - this.origin.getY();
        int z = pos.getZ() - this.origin.getZ();
        IntOpenHashSet set = wideOnly ? this.wide : this.lane;
        if (this.inXZ(x, z) && y >= 0 && y < this.sy && set.contains(this.idx(x, y, z))) {
            return this.idx(x, y, z);
        }
        return this.nearestIn(set, x, y, z, 3);
    }

    /** Lane steps from {@code cell} to the pad, or -1 when the cell is not in that pad's field. */
    public int distance(int pad, boolean wideOnly, int cell) {
        if (!this.ready || cell < 0 || pad < 0 || pad >= this.pads.size()) {
            return -1;
        }
        return (wideOnly ? this.wideFields : this.fields).get(pad).get(cell);
    }

    /** Lane steps from a cave mouth to a pad, or -1 when unreachable. */
    public int caveDistance(int cave, int pad, boolean wideOnly) {
        if (!this.ready) {
            return -1;
        }
        return this.distance(pad, wideOnly, wideOnly ? this.caveWideCells[cave] : this.caveCells[cave]);
    }

    /** The pads a cave faces: one pad, or two that are equally near (the cave alternates between them). */
    public int[] caveHomes(int cave) {
        return this.ready ? this.caveHomes[cave] : new int[0];
    }

    public boolean isCaveUsable(int cave) {
        return this.ready && !this.badCaves.contains(cave);
    }

    /**
     * The next waypoint towards the pad: walks the field downhill up to {@code lookahead} steps, breaking ties between
     * equal neighbours by {@code salt}, and returns the farthest cell on that walk still in straight-line view of the
     * start. Returns null when the cell is not in the field or already at the pad.
     */
    public BlockPos waypoint(int pad, boolean wideOnly, int cell, int lookahead, int salt) {
        if (!this.ready || cell < 0) {
            return null;
        }
        Int2IntOpenHashMap field = (wideOnly ? this.wideFields : this.fields).get(pad);
        IntOpenHashSet allowed = wideOnly ? this.wide : this.lane;
        int d = field.get(cell);
        if (d <= 0) {
            return null;
        }
        IntArrayList path = new IntArrayList();
        path.add(cell);
        int current = cell;
        for (int step = 0; step < lookahead && field.get(current) > 0; step++) {
            IntArrayList downhill = new IntArrayList();
            int here = field.get(current);
            this.forEachMove(current, MOB_UP, MOB_DOWN, allowed, next -> {
                int nd = field.get(next);
                if (nd >= 0 && nd < here) {
                    downhill.add(next);
                }
            });
            if (downhill.isEmpty()) {
                break;
            }
            current = downhill.getInt(Math.floorMod(salt + step, downhill.size()));
            path.add(current);
        }
        int pick = path.size() - 1;
        while (pick > 1 && !this.inView(allowed, path.getInt(0), path.getInt(pick))) {
            pick--;
        }
        return this.toWorld(path.getInt(Math.max(1, Math.min(pick, path.size() - 1))));
    }

    /** One step downhill from {@code cell}, for nudging a stuck mob; null when there is none. */
    public BlockPos nextStep(int pad, boolean wideOnly, int cell) {
        if (!this.ready || cell < 0) {
            return null;
        }
        Int2IntOpenHashMap field = (wideOnly ? this.wideFields : this.fields).get(pad);
        int here = field.get(cell);
        if (here <= 0) {
            return null;
        }
        int[] best = {-1, here};
        this.forEachMove(cell, MOB_UP, MOB_DOWN, wideOnly ? this.wide : this.lane, next -> {
            int nd = field.get(next);
            if (nd >= 0 && nd < best[1]) {
                best[0] = next;
                best[1] = nd;
            }
        });
        return best[0] < 0 ? null : this.toWorld(best[0]);
    }

    private boolean inView(IntOpenHashSet allowed, int from, int to) {
        int x0 = this.cx(from);
        int y0 = this.cy(from);
        int z0 = this.cz(from);
        int x1 = this.cx(to);
        int y1 = this.cy(to);
        int z1 = this.cz(to);
        int n = Math.max(Math.abs(x1 - x0), Math.abs(z1 - z0)) * 2 + 1;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            int x = (int) Math.round(x0 + (x1 - x0) * t);
            int z = (int) Math.round(z0 + (z1 - z0) * t);
            double y = y0 + (y1 - y0) * t;
            boolean ok = false;
            if (this.inXZ(x, z)) {
                for (int ny : this.columns[x * this.sz + z]) {
                    if (Math.abs(ny - y) <= 1.01D && allowed.contains(this.idx(x, ny, z))) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /** A random cache chest cell (feet position, world coordinates), or null when the room has none. */
    public BlockPos randomChestSpot(java.util.Random random) {
        if (this.chestCandidates.length == 0) {
            return null;
        }
        return this.toWorld(this.chestCandidates[random.nextInt(this.chestCandidates.length)]);
    }

    /** True when {@code pos} (world coordinates, feet position) is a cache chest cell. */
    public boolean isChestSpot(BlockPos pos) {
        int x = pos.getX() - this.origin.getX();
        int y = pos.getY() - this.origin.getY();
        int z = pos.getZ() - this.origin.getZ();
        if (!this.inXZ(x, z) || y < 0 || y >= this.sy) {
            return false;
        }
        int cell = this.idx(x, y, z);
        for (int candidate : this.chestCandidates) {
            if (candidate == cell) {
                return true;
            }
        }
        return false;
    }

    public int chestSpotCount() {
        return this.chestCandidates.length;
    }

    /** Every chest cell within {@code radius} columns of {@code centre}, in world coordinates. */
    public List<BlockPos> chestSpotsNear(BlockPos centre, int radius) {
        List<BlockPos> out = new ArrayList<>();
        for (int cell : this.chestCandidates) {
            BlockPos pos = this.toWorld(cell);
            if (Math.abs(pos.getX() - centre.getX()) <= radius && Math.abs(pos.getZ() - centre.getZ()) <= radius
                    && Math.abs(pos.getY() - centre.getY()) <= radius) {
                out.add(pos);
            }
        }
        return out;
    }

    private BlockPos toWorld(int cell) {
        return new BlockPos(this.origin.getX() + this.cx(cell), this.origin.getY() + this.cy(cell), this.origin.getZ() + this.cz(cell));
    }

    private boolean pass(int x, int y, int z) {
        return y >= 0 && y < this.sy && this.passable.get(this.idx(x, y, z));
    }

    private boolean inXZ(int x, int z) {
        return x >= 0 && x < this.sx && z >= 0 && z < this.sz;
    }

    private int idx(int x, int y, int z) {
        return (x * this.sy + y) * this.sz + z;
    }

    private int cx(int cell) {
        return cell / (this.sy * this.sz);
    }

    private int cy(int cell) {
        return (cell / this.sz) % this.sy;
    }

    private int cz(int cell) {
        return cell % this.sz;
    }
}

package xyz.iwolfking.woldsvaults.api.core.layout.impl;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.GridLayout;

public class ClassicSerpentineLayout extends ClassicInfiniteLayout {

    public static final SupplierKey<GridLayout> KEY =
            SupplierKey.of("classic_serpentine_vault", GridLayout.class)
                    .with(Version.v1_0, ClassicSerpentineLayout::new);

    public static final FieldRegistry FIELDS =
            ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> WIDTH =
            FieldKey.of("width", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> ROW_STEP =
            FieldKey.of("row_step", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> BRANCH_INTERVAL =
            FieldKey.of("branch_interval", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    protected ClassicSerpentineLayout() {
        super(1);
        this.set(WIDTH, 4);
        this.set(ROW_STEP, 2);
        this.set(BRANCH_INTERVAL, 3);
    }

    public ClassicSerpentineLayout(int tunnelSpan, int width, int rowStep, int branchInterval) {
        super(tunnelSpan);
        this.set(WIDTH, width);
        this.set(ROW_STEP, rowStep);
        this.set(BRANCH_INTERVAL, branchInterval);
    }

    @Override
    public SupplierKey<GridLayout> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    protected boolean isRoomAllowed(Vault vault, int x, int z) {

        // Start room always allowed
        if (x == 0 && z == 0) {
            return true;
        }

        int width  = this.get(WIDTH);

        if (z < 0 || z >= width) {
            return false;
        }

        boolean leftToRight = (z % 2 == 0);

        int minX = leftToRight ? 0 : -(width - 1);
        int maxX = leftToRight ? (width - 1) : 0;

        // === Spine ===
        if (x >= minX && x <= maxX) {
            return true;
        }

        // === Branches ===
        int branchSpacing = this.get(ROW_STEP);
        int branchLength  = this.get(BRANCH_INTERVAL);

        // Only branch off at regular spine intervals
        int localX = leftToRight ? x : -x;
        if (localX < 0 || localX >= width) {
            return false;
        }

        if (localX % branchSpacing != 0) {
            return false;
        }

        // Branch direction alternates by row
        int branchDir = (z % 4 < 2) ? 1 : -1;

        int dz = z + branchDir;

        // Prevent branch collision with adjacent rows
        if (dz < 0 || dz >= width) {
            return false;
        }

        int distance = Math.abs(z - dz);
        return distance <= branchLength;
    }

    @Override
    public PieceType getType(Vault vault, RegionPos region) {
        int unit = this.get(TUNNEL_SPAN) + 1;
        int x = region.getX();
        int z = region.getZ();

        if (!isRoomAllowed(vault, x / unit, z / unit)) {
            return PieceType.NONE;
        }

        PieceType type = super.getType(vault, region);

        if (type == PieceType.TUNNEL_X) {
            int xRoom1 = x - Math.floorMod(x, unit);
            int xRoom2 = xRoom1 + unit;
            if (!this.getType(vault, region.with(xRoom1, z)).connectsToTunnel()) {
                return PieceType.NONE;
            }

            if (!this.getType(vault, region.with(xRoom2, z)).connectsToTunnel()) {
                return PieceType.NONE;
            }
        } else if (type == PieceType.TUNNEL_Z) {
            int zRoom1 = z - Math.floorMod(z, unit);
            int zRoom2 = zRoom1 + unit;
            if (!this.getType(vault, region.with(x, zRoom1)).connectsToTunnel()) {
                return PieceType.NONE;
            }

            if (!this.getType(vault, region.with(x, zRoom2)).connectsToTunnel()) {
                return PieceType.NONE;
            }
        }
        return type;
    }
}

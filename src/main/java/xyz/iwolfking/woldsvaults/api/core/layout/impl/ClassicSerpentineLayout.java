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
import iskallia.vault.core.world.generator.layout.VaultLayout;

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


    private boolean isSnakeRoom(int x, int z, int unit) {
        int rowStep = this.get(ROW_STEP) * unit;

        if (Math.floorMod(z, rowStep) != 0) {
            return false;
        }

        int row = Math.floorDiv(z, rowStep);
        int maxX = this.get(WIDTH) * unit;

        if ((row & 1) == 0) {
            return x >= -maxX && x <= maxX;
        } else {
            return x >= -maxX && x <= maxX;
        }
    }

    private boolean isBranchRoom(int x, int z, int unit) {
        int rowStep = this.get(ROW_STEP) * unit;

        if (Math.floorMod(z, rowStep) != 0) {
            return false;
        }

        int row = Math.floorDiv(z, rowStep);
        int interval = this.get(BRANCH_INTERVAL);

        if (interval <= 0 || Math.abs(row) % interval != 0) {
            return false;
        }

        int maxX = this.get(WIDTH) * unit;
        return x == maxX + unit || x == -maxX - unit;
    }

    private boolean isBridgeRoom(int x, int z, int unit) {
        int rowStep = this.get(ROW_STEP) * unit;

        // Middle room between snake rows
        if (Math.floorMod(z, rowStep) != unit) {
            return false;
        }

        int maxX = this.get(WIDTH) * unit;
        return x >= -maxX && x <= maxX;
    }


    private boolean isValidRoom(int x, int z, int unit) {
        return isSnakeRoom(x, z, unit)
                || isBridgeRoom(x, z, unit)
                || isBranchRoom(x, z, unit);
    }


    @Override
    public VaultLayout.PieceType getType(Vault vault, RegionPos region) {
        int x = region.getX();
        int z = region.getZ();
        int unit = this.get(TUNNEL_SPAN) + 1;

        if (!isValidRoom(x, z, unit)) {
            return VaultLayout.PieceType.NONE;
        }

        VaultLayout.PieceType type = super.getType(vault, region);

        if (type.isTunnel()) {
            int nx = x;
            int nz = z;

            if (type == VaultLayout.PieceType.TUNNEL_X) {
                nx += unit;
            } else if (type == VaultLayout.PieceType.TUNNEL_Z) {
                nz += unit;
            }

            if (!isValidRoom(nx, nz, unit)) {
                return VaultLayout.PieceType.NONE;
            }
        }

        return type;
    }
}

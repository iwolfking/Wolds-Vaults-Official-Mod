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

public class ClassicTunnelLayout extends ClassicInfiniteLayout {

    public static final SupplierKey<GridLayout> KEY =
            SupplierKey.of("classic_tunnel_vault", GridLayout.class)
                    .with(Version.v1_0, ClassicTunnelLayout::new);

    public static final FieldRegistry FIELDS =
            ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> WIDTH =
            FieldKey.of("width", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> HEIGHT =
            FieldKey.of("height", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> BRANCH_INTERVAL =
            FieldKey.of("branch_interval", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public ClassicTunnelLayout() {
        super(1);
        this.set(WIDTH, 6);
        this.set(HEIGHT, 10);
        this.set(BRANCH_INTERVAL, 10);
    }

    public ClassicTunnelLayout(int tunnel, int width, int rowStep, int branchInterval) {
        super(tunnel);
        this.set(WIDTH, width);
        this.set(HEIGHT, rowStep);
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

    protected boolean isRoomAllowed(int x, int z) {

        int width  = this.get(WIDTH);
        int length = this.get(HEIGHT);

        if (Math.abs(z) >= length) return false;

        if (x == 0 && z == 0) return true;

        if ((Math.abs(x) == 1 && z == 0) || (Math.abs(z) == 1 && x == 0)) {
            return true;
        }

        boolean leftToRight = (z % 2 == 0);

        int minX = leftToRight ? 1 : 1 - width;
        int maxX = leftToRight ? width : 1;

        if (x >= minX && x <= maxX) return true;

        if (z > 0) {
            int connectorX = leftToRight ? minX : maxX;
            if (x == connectorX) return true;
        }

        int branchEvery = this.get(BRANCH_INTERVAL);
        if (branchEvery > 0 && z % branchEvery == 0) {
            if (x == minX - 1 || x == maxX + 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public PieceType getType(Vault vault, RegionPos region) {
        int unit = this.get(TUNNEL_SPAN) + 1;
        int rx = region.getX() / unit;
        int rz = region.getZ() / unit;

        // Only allow rooms that actually exist
        if (!isRoomAllowed(rx, rz)) {
            return PieceType.NONE;
        }

        PieceType type = super.getType(vault, region);

        // Check horizontal neighbors if this is a horizontal tunnel
        if (type == PieceType.TUNNEL_X) {
            int x1 = region.getX() - Math.floorMod(region.getX(), unit);
            int x2 = x1 + unit;

            int rx1 = x1 / unit;
            int rx2 = x2 / unit;

            if (!isRoomAllowed(rx1, rz) || !super.getType(vault, region.with(x1, region.getZ())).connectsToTunnel())
                return PieceType.NONE;
            if (!isRoomAllowed(rx2, rz) || !super.getType(vault, region.with(x2, region.getZ())).connectsToTunnel())
                return PieceType.NONE;
        }

        // Check vertical neighbors if this is a vertical tunnel
        if (type == PieceType.TUNNEL_Z) {
            int z1 = region.getZ() - Math.floorMod(region.getZ(), unit);
            int z2 = z1 + unit;

            int rz1 = z1 / unit;
            int rz2 = z2 / unit;

            if (!isRoomAllowed(rx, rz1) || !super.getType(vault, region.with(region.getX(), z1)).connectsToTunnel())
                return PieceType.NONE;
            if (!isRoomAllowed(rx, rz2) || !super.getType(vault, region.with(region.getX(), z2)).connectsToTunnel())
                return PieceType.NONE;
        }

        return type;
    }

}

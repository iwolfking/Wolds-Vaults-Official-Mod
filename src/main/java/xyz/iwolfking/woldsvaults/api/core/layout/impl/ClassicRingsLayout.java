package xyz.iwolfking.woldsvaults.api.core.layout.impl;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.ClassicCircleLayout;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;

public class ClassicRingsLayout extends ClassicInfiniteLayout {

    public static final SupplierKey<GridLayout> KEY =
            SupplierKey.of("classic_rings_vault", GridLayout.class)
                    .with(Version.v1_0, ClassicRingsLayout::new);

    public static final FieldRegistry FIELDS =
            ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> RADIUS =
            FieldKey.of("radius", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> RING_INTERVAL =
            FieldKey.of("ring_interval", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public ClassicRingsLayout() {
        super(1);
        this.set(RADIUS, 6);
        this.set(RING_INTERVAL, 2);
    }

    public ClassicRingsLayout(int tunnel, int radius, int ringInterval) {
        super(tunnel);
        this.set(RADIUS, radius);
        this.set(RING_INTERVAL, ringInterval);
    }

    @Override
    public SupplierKey<GridLayout> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    public boolean isInRing(RegionPos pos) {
        int unit = this.get(TUNNEL_SPAN) + 1;
        int radius = this.get(RADIUS);
        int ringInterval = this.get(RING_INTERVAL);
        int numRings = Math.max(1, radius / ringInterval);

        int rx = pos.getX() / unit;
        int rz = pos.getZ() / unit;

        int dist = Math.max(Math.abs(rx), Math.abs(rz));

        if (dist == 0) return true;

        if (dist > numRings) return false;

        if (rx == 0 || rz == 0) return true;

        if (dist % ringInterval == 0) return true;

        return false;
    }


    public VaultLayout.PieceType getType(Vault vault, RegionPos region) {
        if (this.has(RADIUS)) {
            int x = region.getX();
            int z = region.getZ();
            int unit = this.get(TUNNEL_SPAN) + 1;

            if (!isInRing(region)) {
                return PieceType.NONE;
            } else {
                VaultLayout.PieceType type = super.getType(vault, region);
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
        } else {
            return super.getType(vault, region);
        }
    }
}

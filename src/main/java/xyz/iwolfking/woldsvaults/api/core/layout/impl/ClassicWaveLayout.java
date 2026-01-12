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
import iskallia.vault.core.world.generator.layout.VaultLayout.PieceType;
import net.minecraft.core.Direction;

public class ClassicWaveLayout extends ClassicInfiniteLayout {

    public static final SupplierKey<GridLayout> KEY =
            SupplierKey.of("classic_wave_vault", GridLayout.class)
                    .with(Version.v1_0, ClassicWaveLayout::new);

    public static final FieldRegistry FIELDS =
            ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> LENGTH =
            FieldKey.of("length", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Integer> AMPLITUDE =
            FieldKey.of("amplitude", Integer.class)
                    .with(Version.v1_0, Adapters.INT, DISK.all())
                    .register(FIELDS);

    public static final FieldKey<Double> FREQUENCY =
            FieldKey.of("frequency", Double.class)
                    .with(Version.v1_0, Adapters.DOUBLE, DISK.all())
                    .register(FIELDS);

    public ClassicWaveLayout() {
        super(1);
        this.set(LENGTH, 12);
        this.set(AMPLITUDE, 3);
        this.set(FREQUENCY, Math.PI / 6.0);
    }

    public ClassicWaveLayout(int tunnel, int length, int amplitude, double frequency) {
        super(tunnel);
        this.set(LENGTH, length);
        this.set(AMPLITUDE, amplitude);
        this.set(FREQUENCY, frequency);
    }

    @Override
    public SupplierKey<GridLayout> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    boolean isRoomAllowed(int x, int z) {
        int length = this.get(LENGTH);
        int amplitude = this.get(AMPLITUDE);
        double frequency = this.get(FREQUENCY);

        if (x == 0 && z == 0) return true; // start room

        for (Direction dir : Direction.values()) { // NESW
            int forward = 0, perpendicular = 0;

            perpendicular = switch (dir) {
                case NORTH -> {
                    forward = -z;
                    yield x;
                }
                case SOUTH -> {
                    forward = z;
                    yield x;
                }
                case EAST -> {
                    forward = x;
                    yield z;
                }
                case WEST -> {
                    forward = -x;
                    yield z;
                }
                default -> perpendicular;
            };

            if (forward < 0 || forward > length) continue;

            int wave = (int) Math.round(amplitude * Math.sin(frequency * forward));
            if (perpendicular == wave) return true;          // wave path
            if (perpendicular == wave + 1 || perpendicular == wave - 1) return true; // side branch
        }

        return false;
    }

    @Override
    public PieceType getType(Vault vault, RegionPos region) {
        int unit = this.get(TUNNEL_SPAN) + 1;

        int rx = region.getX() / unit;
        int rz = region.getZ() / unit;

        if (!isRoomAllowed(rx, rz)) return PieceType.NONE;

        PieceType type = super.getType(vault, region);

        // check neighbors along X
        if (type == PieceType.TUNNEL_X) {
            int x1 = region.getX() - Math.floorMod(region.getX(), unit);
            int x2 = x1 + unit;

            if (!isRoomAllowed(x1 / unit, rz) ||
                !super.getType(vault, region.with(x1, region.getZ())).connectsToTunnel()) return PieceType.NONE;
            if (!isRoomAllowed(x2 / unit, rz) ||
                !super.getType(vault, region.with(x2, region.getZ())).connectsToTunnel()) return PieceType.NONE;
        }

        // check neighbors along Z
        if (type == PieceType.TUNNEL_Z) {
            int z1 = region.getZ() - Math.floorMod(region.getZ(), unit);
            int z2 = z1 + unit;

            if (!isRoomAllowed(rx, z1 / unit) ||
                !super.getType(vault, region.with(region.getX(), z1)).connectsToTunnel()) return PieceType.NONE;
            if (!isRoomAllowed(rx, z2 / unit) ||
                !super.getType(vault, region.with(region.getX(), z2)).connectsToTunnel()) return PieceType.NONE;
        }

        return type;
    }
}

package xyz.iwolfking.woldsvaults.api.vault.layout;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.compound.IntList;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;

public class ClassicStarLayout extends ClassicInfiniteLayout {

    public static final SupplierKey<GridLayout> KEY =
        SupplierKey.of("classic_star_vault", GridLayout.class)
            .with(Version.v1_0, ClassicStarLayout::new);

    public static final FieldRegistry FIELDS =
        ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<IntList> VERTICES =
        FieldKey.of("vertices", IntList.class)
            .with(Version.v1_0, CompoundAdapter.of(() -> IntList.createSegmented(10)), DISK.all())
            .register(FIELDS);

    protected ClassicStarLayout() {}

    public ClassicStarLayout(int tunnelSpan, int arms, int outerRadius, int innerRadius) {
        super(tunnelSpan);
        this.set(VERTICES, generateStar(arms, outerRadius, innerRadius));
    }

    @Override
    public SupplierKey<GridLayout> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    public VaultLayout.PieceType getType(Vault vault, RegionPos region) {
        int unit = this.get(TUNNEL_SPAN) + 1;
        int x = region.getX() / unit;
        int z = region.getZ() / unit;

        if (!containsPoint(x, z)) {
            return VaultLayout.PieceType.NONE;
        }

        return super.getType(vault, region);
    }

    private boolean containsPoint(int x, int z) {
        IntList vertices = this.get(VERTICES);
        int n = vertices.size() / 2;

        int[] xp = new int[n];
        int[] zp = new int[n];

        for (int i = 0; i < n; i++) {
            xp[i] = vertices.get(i * 2);
            zp[i] = vertices.get(i * 2 + 1);
        }

        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if ((zp[i] > z) != (zp[j] > z) &&
                x < (xp[j] - xp[i]) * (z - zp[i]) / (zp[j] - zp[i]) + xp[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static IntList generateStar(int arms, int outer, int inner) {
        IntList list = IntList.createSegmented(arms * 4);
        double step = Math.PI / arms;

        for (int i = 0; i < arms * 2; i++) {
            double angle = i * step;
            int radius = (i % 2 == 0) ? outer : inner;

            int x = (int)Math.round(Math.cos(angle) * radius);
            int z = (int)Math.round(Math.sin(angle) * radius);

            list.add(x);
            list.add(z);
        }
        return list;
    }
}

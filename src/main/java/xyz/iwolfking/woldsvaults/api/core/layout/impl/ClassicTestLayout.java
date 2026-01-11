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

public class ClassicTestLayout extends ClassicInfiniteLayout {
   public static final SupplierKey<GridLayout> KEY = SupplierKey.of("classic_test_vault", GridLayout.class).with(Version.v1_0, ClassicTestLayout::new);
   public static final FieldRegistry FIELDS = ClassicInfiniteLayout.FIELDS.merge(new FieldRegistry());
   public static final FieldKey<Integer> RADIUS = FieldKey.of("radius", Integer.class).with(Version.v1_0, Adapters.INT, DISK.all()).register(FIELDS);

   protected ClassicTestLayout() {
      super(1);
      this.set(RADIUS, 6);
   }

   public ClassicTestLayout(int tunnelSpan, int radius) {
      super(tunnelSpan);
      this.set(RADIUS, radius);
   }

   @Override
   public SupplierKey<GridLayout> getKey() {
      return KEY;
   }

   @Override
   public FieldRegistry getFields() {
      return FIELDS;
   }

   private boolean isStarRoom(int x, int z, int max) {
      return (z == 0 && Math.abs(x) <= max)
              || (x == 0 && Math.abs(z) <= max);
   }


   @Override
   public VaultLayout.PieceType getType(Vault vault, RegionPos region) {
      int x = region.getX();
      int z = region.getZ();

      int unit = this.get(TUNNEL_SPAN) + 1;
      int max = this.get(RADIUS) * unit;

      boolean onXAxis = z == 0 && Math.abs(x) <= max;
      boolean onZAxis = x == 0 && Math.abs(z) <= max;

      // If not part of the star arms, nothing exists here
      if (!onXAxis && !onZAxis) {
         return VaultLayout.PieceType.NONE;
      }

      // Ask Classic layout what *would* be here
      VaultLayout.PieceType type = super.getType(vault, region);

      // Extra tunnel safety: only allow tunnels that connect valid rooms
      if (type == VaultLayout.PieceType.TUNNEL_X) {
         int xRoom1 = x - Math.floorMod(x, unit);
         int xRoom2 = xRoom1 + unit;

         if (!isStarRoom(xRoom1, z, max) || !isStarRoom(xRoom2, z, max)) {
            return VaultLayout.PieceType.NONE;
         }
      }

      if (type == VaultLayout.PieceType.TUNNEL_Z) {
         int zRoom1 = z - Math.floorMod(z, unit);
         int zRoom2 = zRoom1 + unit;

         if (!isStarRoom(x, zRoom1, max) || !isStarRoom(x, zRoom2, max)) {
            return VaultLayout.PieceType.NONE;
         }
      }

      return type;
   }

}
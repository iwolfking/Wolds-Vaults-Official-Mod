package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

// Where vault pockets live: everything east of x = 2^20 in a vault world belongs to pockets, and the vault's own layout never
// generates there. Each pocket gets a 2048-wide slot and is built from the slot's middle, so one pocket's view distance never
// reaches another's or the vault's edge. Client and server both work it out from coordinates alone.
public final class PocketTerritory {
    public static final int START_X = 1 << 20;
    public static final int SLOT_WIDTH = 2048;
    // the height vault rooms are built at, so a room used as a pocket sits the same way it does in the vault
    public static final int BASE_Y = 9;

    private PocketTerritory() {
    }

    public static boolean isVaultWorld(Level level) {
        ResourceLocation dimension = level.dimension().location();
        return dimension.getNamespace().equals("the_vault") && dimension.getPath().startsWith("vault_");
    }

    public static boolean inTerritory(double x) {
        return x >= START_X;
    }

    public static boolean inPocket(Entity entity) {
        return entity != null && isVaultWorld(entity.level) && inTerritory(entity.getX());
    }

    public static int slotOf(int x) {
        return Math.floorDiv(x - START_X, SLOT_WIDTH);
    }

    public static BlockPos origin(int slot) {
        return new BlockPos(START_X + slot * SLOT_WIDTH + SLOT_WIDTH / 2, BASE_Y, 0);
    }
}

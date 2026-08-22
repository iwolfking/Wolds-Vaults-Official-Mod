package xyz.iwolfking.woldsvaults.loot;

/**
 * Slot geometry for chests generated inside a vault.
 *
 * <p>An in-vault chest always opens vanilla's fixed three-row chest screen, whatever its container
 * holds: {@code VaultChestBlock.getMenuProvider} only picks a size-matched menu for chests that are
 * not vault chests, so in-vault chests fall through to {@code ChestBlockEntity.createMenu} and its
 * hardcoded {@code ChestMenu.threeRows}. Every slot from {@link #VISIBLE} up therefore exists,
 * persists and drops when the chest is broken, but never appears on screen.
 *
 * <p>Loot fills the visible slots first and only spills into the hidden ones once they are full,
 * so a chest that generates more stacks than the screen can show keeps the surplus instead of
 * discarding it when the slot list runs out.
 */
public final class VaultChestSlots {
    /** Slots an in-vault chest holds, visible and hidden together. */
    public static final int IN_VAULT = 54;

    /** Slots of an in-vault chest the three-row chest screen shows. */
    public static final int VISIBLE = 27;

    private VaultChestSlots() {
    }
}

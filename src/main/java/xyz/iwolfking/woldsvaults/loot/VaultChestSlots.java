package xyz.iwolfking.woldsvaults.loot;

/**
 * Slot geometry for chests generated inside a vault. An in-vault chest always opens vanilla's fixed
 * three-row screen, so every slot from {@link #VISIBLE} up exists and drops when the chest breaks
 * but never appears on screen; loot fills the visible slots first.
 */
public final class VaultChestSlots {
    /** Slots an in-vault chest holds, visible and hidden together. */
    public static final int IN_VAULT = 54;

    public static final int VISIBLE = 27;

    private VaultChestSlots() {
    }
}

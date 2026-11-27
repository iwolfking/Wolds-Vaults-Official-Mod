package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

/** A spell slot key, unbound by default, that sorts by slot number in the Controls list, so 2 comes before 10. */
public final class SlotKeyMapping extends KeyMapping {
    private final int slot;

    public SlotKeyMapping(int slot, String name, String category) {
        super(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category);
        this.slot = slot;
    }

    @Override
    public int compareTo(KeyMapping other) {
        if (other instanceof SlotKeyMapping slotKey) {
            return Integer.compare(this.slot, slotKey.slot);
        }
        return super.compareTo(other);
    }
}

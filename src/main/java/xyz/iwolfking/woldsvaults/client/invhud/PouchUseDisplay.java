package xyz.iwolfking.woldsvaults.client.invhud;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchUseTotals;

public final class PouchUseDisplay {
    private static final Map<PouchContents, Map<ItemStack, PouchUseTotals>> TICK_TOTALS = new IdentityHashMap<>();
    private static long cachedTick = Long.MIN_VALUE;

    private PouchUseDisplay() {}

    @Nullable
    private static PouchUseTotals pooledUses(PouchContents contents, ItemStack stack) {
        if (!contents.autoReplace()) return null;
        long tick = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        if (tick != cachedTick) {
            TICK_TOTALS.clear();
            cachedTick = tick;
        }
        return TICK_TOTALS.computeIfAbsent(contents, PouchUseTotals::collect).get(stack);
    }

    public static long remainingUses(PouchContents contents, ItemStack stack) {
        PouchUseTotals pooled = pooledUses(contents, stack);
        return pooled == null ? PouchHelper.remainingUses(stack) : pooled.remaining();
    }

    @Nullable
    public static PouchUseTotals equippedUses(Player player, ItemStack stack) {
        ItemStack pouch = PouchHelper.equipped(player);
        return PouchHelper.isPouch(pouch) ? pooledUses(PouchCapability.get(pouch), stack) : null;
    }
}

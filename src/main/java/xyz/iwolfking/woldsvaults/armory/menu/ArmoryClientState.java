package xyz.iwolfking.woldsvaults.armory.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.SavedBuild;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The open page as the client draws it: slot marks, the saved layout (ghosts and saved-empty slots), the H-screen
 * stat snapshot from the Save, and the saved level. Stored items reach the client through the slot sync instead.
 */
public record ArmoryClientState(Set<String> marks, Map<String, ItemStack> expected, CompoundTag stats, boolean hasBuild,
                                int savedLevel, String saver) {
    public static final ArmoryClientState EMPTY = new ArmoryClientState(Set.of(), Map.of(), new CompoundTag(), false, 0, "");

    public static ArmoryClientState of(ArmoryPage page) {
        SavedBuild build = page.build();
        return new ArmoryClientState(new LinkedHashSet<>(page.marks()), new HashMap<>(page.expectedMap()),
                build == null ? new CompoundTag() : build.stats(), build != null, build == null ? 0 : build.level(),
                build == null ? "" : build.saver());
    }

    public boolean isMarked(String key) {
        return this.marks.contains(key);
    }

    /** Null when the slot has no saved layout, EMPTY when it was saved empty, otherwise the ghost. */
    @Nullable
    public ItemStack expected(String key) {
        return this.expected.get(key);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.marks.size());
        this.marks.forEach(buf::writeUtf);
        buf.writeVarInt(this.expected.size());
        this.expected.forEach((key, stack) -> {
            buf.writeUtf(key);
            buf.writeItem(stack);
        });
        buf.writeNbt(this.stats);
        buf.writeBoolean(this.hasBuild);
        buf.writeVarInt(this.savedLevel);
        buf.writeUtf(this.saver);
    }

    public static ArmoryClientState read(FriendlyByteBuf buf) {
        int markCount = buf.readVarInt();
        Set<String> marks = new LinkedHashSet<>();
        for (int i = 0; i < markCount; i++) {
            marks.add(buf.readUtf());
        }
        int expectedCount = buf.readVarInt();
        Map<String, ItemStack> expected = new HashMap<>();
        for (int i = 0; i < expectedCount; i++) {
            String key = buf.readUtf();
            expected.put(key, buf.readItem());
        }
        CompoundTag stats = buf.readNbt();
        return new ArmoryClientState(marks, expected, stats == null ? new CompoundTag() : stats, buf.readBoolean(),
                buf.readVarInt(), buf.readUtf());
    }
}

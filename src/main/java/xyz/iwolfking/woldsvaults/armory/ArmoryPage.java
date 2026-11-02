package xyz.iwolfking.woldsvaults.armory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * One armory build page: the saved trees, the slot marks, the saved layout ({@code expected}: a ghost copy, or
 * {@link ItemStack#EMPTY} for "saved empty", or absent for unset) and the real items the page holds.
 */
public final class ArmoryPage {
    @Nullable
    private SavedBuild build;
    private final Set<String> marks = new LinkedHashSet<>();
    private final Map<String, ItemStack> expected = new HashMap<>();
    private final Map<String, ItemStack> stored = new HashMap<>();

    public ArmoryPage() {
        for (String key : SlotKeys.ALL) {
            if (SlotKeys.markedByDefault(key)) {
                this.marks.add(key);
            }
        }
    }

    @Nullable
    public SavedBuild build() {
        return this.build;
    }

    public void setBuild(@Nullable SavedBuild build) {
        this.build = build;
    }

    public boolean hasSave() {
        return this.build != null || !this.expected.isEmpty();
    }

    public boolean isMarked(String key) {
        return this.marks.contains(key);
    }

    public void toggleMark(String key) {
        if (!this.marks.remove(key)) {
            this.marks.add(key);
        }
    }

    public Set<String> marks() {
        return this.marks;
    }

    public ItemStack stored(String key) {
        return this.stored.getOrDefault(key, ItemStack.EMPTY);
    }

    public void setStored(String key, ItemStack stack) {
        if (stack.isEmpty()) {
            this.stored.remove(key);
        } else {
            this.stored.put(key, stack);
        }
    }

    public Map<String, ItemStack> storedMap() {
        return this.stored;
    }

    /** The saved layout entry: null when unset, EMPTY for saved-empty, otherwise a ghost copy. */
    @Nullable
    public ItemStack expected(String key) {
        return this.expected.get(key);
    }

    public void setExpected(String key, @Nullable ItemStack ghost) {
        if (ghost == null) {
            this.expected.remove(key);
        } else {
            this.expected.put(key, ghost.isEmpty() ? ItemStack.EMPTY : ghost(ghost));
        }
    }

    public Map<String, ItemStack> expectedMap() {
        return this.expected;
    }

    public static ItemStack ghost(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(Math.max(1, stack.getCount()));
        return copy;
    }

    /** The page's weapon for its tab icon: the selected hotbar slot at save, then the first stored gear piece. */
    public ItemStack icon() {
        int selected = 0;
        if (this.build != null && this.build.stats().contains("selected")) {
            selected = this.build.stats().getInt("selected");
        }
        String key = "hotbar:" + selected;
        ItemStack stack = this.stored(key);
        if (stack.isEmpty() && this.expected(key) != null) {
            stack = this.expected(key);
        }
        if (stack.isEmpty()) {
            for (String armor : SlotKeys.ALL.subList(0, SlotKeys.HOTBAR_START)) {
                if (!this.stored(armor).isEmpty()) {
                    return this.stored(armor);
                }
            }
        }
        return stack;
    }

    /**
     * Writes the page. With {@code slim}, ghosts shrink to item id and name and stored items are left out, for the
     * dropped block item ({@code BlockEntityTag} must stay small).
     */
    public CompoundTag save(boolean slim) {
        CompoundTag tag = new CompoundTag();
        if (this.build != null) {
            tag.put("build", this.build.save());
        }
        ListTag marks = new ListTag();
        this.marks.forEach(key -> marks.add(StringTag.valueOf(key)));
        tag.put("marks", marks);
        CompoundTag expected = new CompoundTag();
        this.expected.forEach((key, stack) -> expected.put(key, slim ? slimGhost(stack) : stack.save(new CompoundTag())));
        tag.put("expected", expected);
        if (!slim) {
            CompoundTag stored = new CompoundTag();
            this.stored.forEach((key, stack) -> stored.put(key, stack.save(new CompoundTag())));
            tag.put("stored", stored);
        }
        return tag;
    }

    private static CompoundTag slimGhost(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        if (stack.isEmpty()) {
            return tag;
        }
        ItemStack slim = new ItemStack(stack.getItem());
        if (stack.hasCustomHoverName()) {
            slim.setHoverName(stack.getHoverName());
        }
        return slim.save(tag);
    }

    public static ArmoryPage load(CompoundTag tag) {
        ArmoryPage page = new ArmoryPage();
        if (tag.contains("build", Tag.TAG_COMPOUND)) {
            page.build = SavedBuild.load(tag.getCompound("build"));
        }
        if (tag.contains("marks", Tag.TAG_LIST)) {
            page.marks.clear();
            ListTag marks = tag.getList("marks", Tag.TAG_STRING);
            for (int i = 0; i < marks.size(); i++) {
                page.marks.add(marks.getString(i));
            }
        }
        CompoundTag expected = tag.getCompound("expected");
        for (String key : expected.getAllKeys()) {
            CompoundTag item = expected.getCompound(key);
            if (item.isEmpty()) {
                page.expected.put(key, ItemStack.EMPTY);
                continue;
            }
            ItemStack ghost = ItemStack.of(item);
            if (ghost.isEmpty()) {
                WoldsVaults.LOGGER.warn("Armory: saved item {} for slot {} no longer exists; the slot is now unset",
                        item.getString("id"), key);
                continue;
            }
            page.expected.put(key, ghost);
        }
        CompoundTag stored = tag.getCompound("stored");
        for (String key : stored.getAllKeys()) {
            ItemStack stack = ItemStack.of(stored.getCompound(key));
            if (!stack.isEmpty()) {
                page.stored.put(key, stack);
            }
        }
        return page;
    }
}

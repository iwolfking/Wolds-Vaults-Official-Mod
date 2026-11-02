package xyz.iwolfking.woldsvaults.armory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.ArmoryRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The armory's storage: 8 build pages, the shared 128-slot excess and the two upgrade slots. Nothing is sent in
 * the chunk update tag; page contents reach a player only through the open menu.
 */
public class ArmoryBlockEntity extends BlockEntity {
    public static final int PAGES = 8;
    public static final int EXCESS_SLOTS = 128;
    public static final int SKILLS_UPGRADE_SLOT = 0;
    public static final int GODS_UPGRADE_SLOT = 1;

    private final ArmoryPage[] pages = new ArmoryPage[PAGES];
    private final ItemStackHandler excess = new ItemStackHandler(EXCESS_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            ArmoryBlockEntity.this.setChanged();
        }
    };
    private final ItemStackHandler upgrades = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            ArmoryBlockEntity.this.setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SKILLS_UPGRADE_SLOT ? stack.is(ArmoryRegistry.SKILLS_UPGRADE) : stack.is(ArmoryRegistry.GODS_UPGRADE);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public ArmoryBlockEntity(BlockPos pos, BlockState state) {
        super(ArmoryRegistry.ARMORY_ENTITY, pos, state);
        for (int i = 0; i < PAGES; i++) {
            this.pages[i] = new ArmoryPage();
        }
    }

    public boolean isCreative() {
        return this.getBlockState().getBlock() instanceof ArmoryBlock block && block.isCreative();
    }

    public ArmoryPage page(int index) {
        return this.pages[Math.max(0, Math.min(PAGES - 1, index))];
    }

    public ItemStackHandler excess() {
        return this.excess;
    }

    public ItemStackHandler upgrades() {
        return this.upgrades;
    }

    public boolean hasSkillsUpgrade() {
        return this.isCreative() || !this.upgrades.getStackInSlot(SKILLS_UPGRADE_SLOT).isEmpty();
    }

    public boolean hasGodsUpgrade() {
        return this.isCreative() || !this.upgrades.getStackInSlot(GODS_UPGRADE_SLOT).isEmpty();
    }

    /** Everything a broken armory spills: stored page items (normal armory only), excess and upgrades. */
    public List<ItemStack> contentsToDrop() {
        List<ItemStack> out = new ArrayList<>();
        if (!this.isCreative()) {
            for (ArmoryPage page : this.pages) {
                page.storedMap().values().forEach(stack -> out.add(stack.copy()));
            }
            for (int i = 0; i < this.upgrades.getSlots(); i++) {
                out.add(this.upgrades.getStackInSlot(i).copy());
            }
        }
        for (int i = 0; i < this.excess.getSlots(); i++) {
            out.add(this.excess.getStackInSlot(i).copy());
        }
        out.removeIf(ItemStack::isEmpty);
        return out;
    }

    /** The saved builds for the dropped block item: trees, marks and slimmed layout; no items. */
    public CompoundTag slimPagesTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ArmoryPage page : this.pages) {
            list.add(page.save(true));
        }
        tag.put("pages", list);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ArmoryPage page : this.pages) {
            list.add(page.save(false));
        }
        tag.put("pages", list);
        tag.put("excess", this.excess.serializeNBT());
        tag.put("upgrades", this.upgrades.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("pages", Tag.TAG_LIST)) {
            ListTag list = tag.getList("pages", Tag.TAG_COMPOUND);
            for (int i = 0; i < PAGES; i++) {
                this.pages[i] = i < list.size() ? ArmoryPage.load(list.getCompound(i)) : new ArmoryPage();
            }
        }
        if (tag.contains("excess", Tag.TAG_COMPOUND)) {
            this.excess.deserializeNBT(tag.getCompound("excess"));
        }
        if (tag.contains("upgrades", Tag.TAG_COMPOUND)) {
            this.upgrades.deserializeNBT(tag.getCompound("upgrades"));
        }
    }
}

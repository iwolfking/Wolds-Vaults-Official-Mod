package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.nbt.CompoundTag;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import xyz.iwolfking.woldsvaults.blocks.containers.ExtractorAssemblerContainer;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStats;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Slots 0–6 take one part (or identified blueprint, for previewing) each in {@link ExtractorPartType#CORE_PARTS}
 * order, 7–9 take attachments, 10 is the assembled extractor. While an assembled extractor sits in slot 10 it is
 * "docked": the part slots mirror its build as untouchable ghosts and the attachment slots become a live view of
 * its hardpoints. Deliberately exposes no item handler: assembly is a hand operation.
 */
public class ExtractorAssemblerTileEntity extends BaseInventoryTileEntity implements MenuProvider, ExtractorStationTile {
    public static final int PART_SLOTS = ExtractorPartType.CORE_PARTS.length;
    public static final int ATTACHMENT_SLOT_START = PART_SLOTS;
    public static final int ATTACHMENT_SLOTS = 3;
    public static final int OUTPUT_SLOT = ATTACHMENT_SLOT_START + ATTACHMENT_SLOTS;
    public static final int REPAIR_SLOT = OUTPUT_SLOT + 1;
    public static final int SLOT_COUNT = REPAIR_SLOT + 1;
    public static final int WORK_CYCLE_TICKS = 120;
    public static final int PAY_INTERVAL_TICKS = 5;
    public static final String REPAIR_TAG = "Repair";

    private final BaseItemStackHandler inventory;
    private int workingTicks;
    @Nullable
    private ItemStack[] ghostParts;
    @Nullable
    private ItemStack[] ghostAttachments;

    public ExtractorAssemblerTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTOR_ASSEMBLER_TILE_ENTITY_TYPE, pos, state);
        this.inventory = createInventoryHandler(this::onContentsChanged);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ExtractorAssemblerTileEntity tile) {
        if (tile.workingTicks > 0) {
            tile.workingTicks--;
            if (tile.workingTicks == 0) {
                tile.markDirtyAndDispatch();
            }
        }
        if (!level.isClientSide() && level.getGameTime() % PAY_INTERVAL_TICKS == 0) {
            tile.tickRepair();
        }
    }

    /**
     * The docked extractor's repair ledger. A damaged extractor gets a cost for exactly the durability it is
     * missing, recomputed whenever its durability differs from the one the cost was written for; payments already
     * made stay on the extractor and count toward the new cost. Null when nothing damaged is docked.
     */
    @Nullable
    public CompoundTag repairLedger() {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty()) {
            return null;
        }
        CompoundTag data = ExtractorItem.getData(docked);
        int max = data.getInt("MaxDurability");
        int current = data.getInt("Durability");
        if (max <= 0 || current >= max) {
            return null;
        }
        if (!data.contains(REPAIR_TAG)) {
            data.put(REPAIR_TAG, new CompoundTag());
        }
        CompoundTag repair = data.getCompound(REPAIR_TAG);
        if (!CostLedger.hasCost(repair) || repair.getInt("ForDurability") != current) {
            CompoundTag paid = repair.getCompound(CostLedger.PAID).copy();
            List<PartData> parts = ExtractorItem.getBuildParts(docked);
            CostLedger.setCost(repair, ExtractorCosts.repairCost(parts, (double) (max - current) / max));
            repair.put(CostLedger.PAID, paid);
            repair.putInt("ForDurability", current);
        }
        return repair;
    }

    private void tickRepair() {
        CompoundTag ledger = this.repairLedger();
        if (ledger != null && CostLedger.payFromSlot(ledger, this.inventory, REPAIR_SLOT)) {
            this.markDirtyAndDispatch();
        }
    }

    /** Null when the docked extractor may be repaired, else why not. */
    @Nullable
    public String repairRefusal() {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty()) {
            return "Dock an assembled extractor to repair it.";
        }
        CompoundTag data = ExtractorItem.getData(docked);
        if (data.getInt("Durability") >= data.getInt("MaxDurability")) {
            return "The docked extractor is at full durability.";
        }
        CompoundTag ledger = data.getCompound(REPAIR_TAG);
        if (!CostLedger.isComplete(ledger)) {
            return "The repair cost is not fully paid; put materials in the repair slot.";
        }
        return null;
    }

    public void repair(Player player) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        this.repairLedger();
        String refusal = this.repairRefusal();
        if (refusal != null) {
            tell(player, refusal);
            return;
        }
        ItemStack docked = this.getDocked();
        CompoundTag data = ExtractorItem.getData(docked);
        int max = data.getInt("MaxDurability");
        int before = data.getInt("Durability");
        data.remove(REPAIR_TAG);
        ExtractorItem.setDurability(docked, max);
        this.workingTicks = WORK_CYCLE_TICKS;
        this.markDirtyAndDispatch();
        WoldLogger.info("{} repaired an extractor from {} to {} durability at {}.", player.getName().getString(), before, max, this.getBlockPos());
        tell(player, "Repaired " + (max - before) + " durability.");
    }

    private void onContentsChanged() {
        this.invalidateGhosts();
        this.markDirtyAndDispatch();
    }

    public void invalidateGhosts() {
        this.ghostParts = null;
        this.ghostAttachments = null;
    }

    @Override
    public boolean isWorking() {
        return this.workingTicks > 0;
    }

    @Override
    public ItemStack getDisplayExtractor() {
        ItemStack output = this.inventory.getStackInSlot(OUTPUT_SLOT);
        return output.getItem() instanceof ExtractorItem ? output : ItemStack.EMPTY;
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        if (this.inventory.getSlots() < SLOT_COUNT) {
            List<ItemStack> saved = new ArrayList<>();
            for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
                saved.add(this.inventory.getStackInSlot(slot));
            }
            this.inventory.setSize(SLOT_COUNT);
            for (int slot = 0; slot < saved.size(); slot++) {
                this.inventory.setStackInSlot(slot, saved.get(slot));
            }
        }
        this.workingTicks = tag.getInt("Working");
        this.invalidateGhosts();
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Working", this.workingTicks);
    }

    public static BaseItemStackHandler createInventoryHandler(@Nullable Runnable onContentsChanged) {
        BaseItemStackHandler inventory = new BaseItemStackHandler(SLOT_COUNT, onContentsChanged);
        inventory.setDefaultSlotLimit(1);
        inventory.addSlotLimit(REPAIR_SLOT, 64);
        inventory.setSlotValidator((slot, stack) -> slotAccepts(slot, stack));
        return inventory;
    }

    /** Hand-placement rules for the assembly slots; the dock (slot 10) is validated by {@link #canDock} instead. */
    public static boolean slotAccepts(int slot, ItemStack stack) {
        if (slot < PART_SLOTS) {
            ExtractorPartType type = ExtractorPartType.CORE_PARTS[slot];
            return ExtractorPartItem.isPart(stack, type) || ExtractorBlueprintItem.isIdentifiedBlueprint(stack, type);
        }
        if (slot < OUTPUT_SLOT) {
            return ExtractorPartItem.isPart(stack, ExtractorPartType.ATTACHMENT);
        }
        return slot == REPAIR_SLOT;
    }

    public static boolean isDockable(ItemStack stack) {
        return ExtractorItem.isAssembled(stack) && !ExtractorItem.isDeployed(stack);
    }

    public static boolean assemblyEmpty(BaseItemStackHandler inventory) {
        for (int slot = 0; slot < OUTPUT_SLOT; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** An assembled, undeployed extractor may be docked only into an otherwise empty assembler. */
    public static boolean canDock(BaseItemStackHandler inventory, ItemStack stack) {
        return isDockable(stack) && assemblyEmpty(inventory);
    }

    @Nullable
    public static PartData partIn(ItemStack stack) {
        if (stack.getItem() instanceof ExtractorPartItem) {
            return ExtractorPartItem.getPart(stack);
        }
        if (stack.getItem() instanceof ExtractorBlueprintItem) {
            return ExtractorBlueprintItem.getPart(stack);
        }
        return null;
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.woldsvaults.extractor_assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return ExtractorAssemblerContainer.create(windowId, playerInventory, this::isUsableByPlayer, this, this.getBlockPos());
    }

    public ItemStack getDocked() {
        ItemStack output = this.inventory.getStackInSlot(OUTPUT_SLOT);
        return ExtractorItem.isAssembled(output) ? output : ItemStack.EMPTY;
    }

    public boolean isDocked() {
        return !this.getDocked().isEmpty();
    }

    /** The parts to preview: the docked extractor's build, or whatever parts and blueprints sit in the slots. */
    public List<PartData> previewParts() {
        ItemStack docked = this.getDocked();
        if (!docked.isEmpty()) {
            return ExtractorItem.getBuildParts(docked);
        }
        List<PartData> parts = new ArrayList<>();
        for (int slot = 0; slot < PART_SLOTS; slot++) {
            PartData part = partIn(this.inventory.getStackInSlot(slot));
            if (part != null) {
                parts.add(part);
            }
        }
        return parts;
    }

    public List<PartData> previewAttachments() {
        ItemStack docked = this.getDocked();
        if (!docked.isEmpty()) {
            return ExtractorItem.getBuildAttachments(docked);
        }
        List<PartData> attachments = new ArrayList<>();
        for (int slot = ATTACHMENT_SLOT_START; slot < OUTPUT_SLOT; slot++) {
            PartData attachment = ExtractorPartItem.getPart(this.inventory.getStackInSlot(slot));
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    public int blueprintCount() {
        int count = 0;
        for (int slot = 0; slot < PART_SLOTS; slot++) {
            if (this.inventory.getStackInSlot(slot).getItem() instanceof ExtractorBlueprintItem) {
                count++;
            }
        }
        return count;
    }

    public int filledPartSlots() {
        int count = 0;
        for (int slot = 0; slot < PART_SLOTS; slot++) {
            if (!this.inventory.getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** Hardpoints the current build grants: the docked extractor's, or what the placed chassis and core would give. */
    public int hardpoints() {
        ItemStack docked = this.getDocked();
        if (!docked.isEmpty()) {
            return ExtractorItem.getSlots(docked);
        }
        return ExtractorStats.slotsFor(partIn(this.inventory.getStackInSlot(0)), partIn(this.inventory.getStackInSlot(1)));
    }

    /** The docked extractor's part for a part slot, as an untouchable display item; empty when nothing is docked. */
    public ItemStack ghostPart(int slot) {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty() || slot < 0 || slot >= PART_SLOTS) {
            return ItemStack.EMPTY;
        }
        if (this.ghostParts == null) {
            this.ghostParts = new ItemStack[PART_SLOTS];
            for (int i = 0; i < PART_SLOTS; i++) {
                this.ghostParts[i] = ItemStack.EMPTY;
            }
            for (PartData part : ExtractorItem.getBuildParts(docked)) {
                int index = slotFor(part.type);
                ExtractorPartItem item = ModItems.EXTRACTOR_PARTS.get(part.type);
                if (index >= 0 && item != null) {
                    this.ghostParts[index] = ExtractorPartItem.create(item, part);
                }
            }
        }
        return this.ghostParts[slot];
    }

    /** The docked extractor's attachment on a hardpoint, as a display item the player can take out. */
    public ItemStack dockedAttachment(int hardpoint) {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty() || hardpoint < 0 || hardpoint >= ATTACHMENT_SLOTS) {
            return ItemStack.EMPTY;
        }
        if (this.ghostAttachments == null) {
            this.ghostAttachments = new ItemStack[ATTACHMENT_SLOTS];
            for (int i = 0; i < ATTACHMENT_SLOTS; i++) {
                this.ghostAttachments[i] = ItemStack.EMPTY;
            }
            List<PartData> attachments = ExtractorItem.getBuildAttachments(docked);
            ExtractorPartItem item = ModItems.EXTRACTOR_PARTS.get(ExtractorPartType.ATTACHMENT);
            for (int i = 0; i < Math.min(attachments.size(), ATTACHMENT_SLOTS) && item != null; i++) {
                this.ghostAttachments[i] = ExtractorPartItem.create(item, attachments.get(i));
            }
        }
        return this.ghostAttachments[hardpoint];
    }

    /** Null when the attachment may be bolted onto the docked extractor's hardpoint, else the reason it may not. */
    @Nullable
    public String attachmentRefusal(int hardpoint, ItemStack stack) {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty()) {
            return "Dock an assembled extractor first.";
        }
        PartData attachment = ExtractorPartItem.isPart(stack, ExtractorPartType.ATTACHMENT) ? ExtractorPartItem.getPart(stack) : null;
        if (attachment == null) {
            return "Only extractor attachments can be installed on a hardpoint.";
        }
        int slots = ExtractorItem.getSlots(docked);
        if (hardpoint >= slots) {
            return "No hardpoint available on this chassis.";
        }
        List<PartData> attachments = ExtractorItem.getBuildAttachments(docked);
        boolean replacing = hardpoint < attachments.size();
        if (!replacing && attachments.size() >= slots) {
            return "Every hardpoint is occupied.";
        }
        for (int i = 0; i < attachments.size(); i++) {
            if (i != hardpoint && attachments.get(i).attachmentId.equals(attachment.attachmentId)) {
                return attachment.name + " is already installed; attachments do not stack.";
            }
        }
        return null;
    }

    /**
     * Writes an attachment onto the docked extractor's hardpoint (replacing whatever was there) or clears the
     * hardpoint when given an empty stack, then recomposes the extractor's stats. The list stays dense, so
     * clearing a middle hardpoint shifts later attachments down one mount.
     */
    public void setDockedAttachment(int hardpoint, ItemStack stack) {
        ItemStack docked = this.getDocked();
        if (docked.isEmpty()) {
            if (!stack.isEmpty()) {
                WoldLogger.error("Tried to install {} on hardpoint {} of the assembler at {} with nothing docked; ignored.", stack, hardpoint, this.getBlockPos());
            }
            return;
        }
        List<PartData> attachments = ExtractorItem.getBuildAttachments(docked);
        PartData attachment = stack.isEmpty() ? null : ExtractorPartItem.getPart(stack);
        if (attachment == null) {
            if (hardpoint < attachments.size()) {
                attachments.remove(hardpoint);
            }
        } else if (hardpoint < attachments.size()) {
            attachments.set(hardpoint, attachment);
        } else {
            attachments.add(attachment);
        }
        ExtractorItem.rewriteStats(docked, ExtractorItem.getBuildParts(docked), attachments);
        this.invalidateGhosts();
        this.markDirtyAndDispatch();
    }

    /** Takes the attachment off a hardpoint and returns it as an item; empty when the hardpoint was bare. */
    public ItemStack removeDockedAttachment(int hardpoint) {
        ItemStack current = this.dockedAttachment(hardpoint).copy();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.setDockedAttachment(hardpoint, ItemStack.EMPTY);
        return current;
    }

    public static int slotFor(ExtractorPartType type) {
        for (int i = 0; i < ExtractorPartType.CORE_PARTS.length; i++) {
            if (ExtractorPartType.CORE_PARTS[i] == type) {
                return i;
            }
        }
        return -1;
    }

    /** Every reason the Assemble button is refused, in the order the screen lists them; empty means it may fire. */
    public List<String> assemblyRefusals() {
        List<String> reasons = new ArrayList<>();
        if (this.isDocked()) {
            reasons.add("Remove the docked extractor first.");
            return reasons;
        }
        if (!this.inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            reasons.add("Remove the finished extractor first.");
        }
        int missing = PART_SLOTS - this.filledPartSlots();
        if (missing > 0) {
            reasons.add(missing == 1 ? "1 part slot is empty." : missing + " part slots are empty.");
        }
        List<String> blueprints = new ArrayList<>();
        for (int slot = 0; slot < PART_SLOTS; slot++) {
            ItemStack stack = this.inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ExtractorBlueprintItem) {
                PartData part = ExtractorBlueprintItem.getPart(stack);
                blueprints.add(part == null ? "blueprint" : part.name);
            }
        }
        if (!blueprints.isEmpty()) {
            reasons.add("Blueprints must be fabricated at an Extraction Forge first: " + String.join(", ", blueprints) + ".");
        }
        List<PartData> attachments = this.previewAttachments();
        int slots = this.hardpoints();
        if (attachments.size() > slots) {
            int excess = attachments.size() - slots;
            reasons.add("Only " + slots + (slots == 1 ? " hardpoint is" : " hardpoints are") + " available; remove " + excess + (excess == 1 ? " attachment." : " attachments."));
        }
        Set<String> seen = new HashSet<>();
        for (PartData attachment : attachments) {
            if (!seen.add(attachment.attachmentId)) {
                reasons.add(attachment.name + " is installed twice; attachments do not stack.");
            }
        }
        return reasons;
    }

    public void assemble(Player player) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        List<String> refusals = this.assemblyRefusals();
        if (!refusals.isEmpty()) {
            tell(player, refusals.get(0));
            return;
        }
        List<PartData> parts = this.previewParts();
        List<PartData> attachments = this.previewAttachments();
        ItemStack extractor = ExtractorItem.buildFromParts(ModItems.EXTRACTOR, parts, attachments);
        for (int slot = 0; slot < OUTPUT_SLOT; slot++) {
            this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        this.inventory.setStackInSlot(OUTPUT_SLOT, extractor);
        this.workingTicks = WORK_CYCLE_TICKS;
        this.invalidateGhosts();
        this.markDirtyAndDispatch();
        WoldLogger.info("{} assembled an extractor from {} parts and {} attachments at {}.", player.getName().getString(), parts.size(), attachments.size(), this.getBlockPos());
        tell(player, "Extractor assembled.");
    }

    private static void tell(Player player, String message) {
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.YELLOW), false);
    }
}

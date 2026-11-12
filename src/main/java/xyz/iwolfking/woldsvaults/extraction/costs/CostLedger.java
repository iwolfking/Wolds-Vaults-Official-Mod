package xyz.iwolfking.woldsvaults.extraction.costs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts.CostLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A pay-as-you-go cost: the owed lines live under {@code Cost} and what has been paid under {@code Paid} inside a
 * compound the caller owns (a blueprint's data, an extractor's repair record). Paying deletes the inserted items
 * or fluid and adds to the ledger, so arbitrarily large costs can be streamed in by hand, pipe or bus. Nothing is
 * ever refunded; the ledger travels with the item it is written on.
 */
public final class CostLedger {
    public static final String COST = "Cost";
    public static final String PAID = "Paid";

    private CostLedger() {
    }

    public static boolean hasCost(@Nullable CompoundTag root) {
        return root != null && root.contains(COST);
    }

    public static void setCost(CompoundTag root, List<CostLine> lines) {
        root.put(COST, ExtractorCosts.writeLines(lines));
        root.put(PAID, new CompoundTag());
    }

    public static void clear(CompoundTag root) {
        root.remove(COST);
        root.remove(PAID);
    }

    public static List<CostLine> lines(@Nullable CompoundTag root) {
        return root == null ? new ArrayList<>() : ExtractorCosts.readLines(root, COST);
    }

    public static long paid(CompoundTag root, CostLine line) {
        return root.getCompound(PAID).getLong(line.key());
    }

    public static long remaining(CompoundTag root, CostLine line) {
        return Math.max(0L, line.amount() - paid(root, line));
    }

    /** True once every resolvable line is paid; lines naming unregistered items or fluids are skipped (logged). */
    public static boolean isComplete(@Nullable CompoundTag root) {
        if (!hasCost(root)) {
            return false;
        }
        for (CostLine line : lines(root)) {
            boolean resolvable = line.fluid() ? line.fluidType() != null : line.item() != null;
            if (resolvable && remaining(root, line) > 0) {
                return false;
            }
        }
        return true;
    }

    private static void addPaid(CompoundTag root, CostLine line, long amount) {
        CompoundTag paid = root.getCompound(PAID);
        paid.putLong(line.key(), paid.getLong(line.key()) + amount);
        root.put(PAID, paid);
    }

    @Nullable
    private static CostLine openLine(CompoundTag root, @Nullable Item item, @Nullable Fluid fluid) {
        for (CostLine line : lines(root)) {
            if (remaining(root, line) <= 0) {
                continue;
            }
            if (item != null && !line.fluid() && line.item() == item) {
                return line;
            }
            if (fluid != null && line.fluid() && line.fluidType() == fluid) {
                return line;
            }
        }
        return null;
    }

    /** How many of {@code stack} the ledger takes; when not simulating, records them as paid. */
    public static int payItems(@Nullable CompoundTag root, ItemStack stack, boolean simulate) {
        if (!hasCost(root) || stack.isEmpty()) {
            return 0;
        }
        CostLine line = openLine(root, stack.getItem(), null);
        if (line == null) {
            return 0;
        }
        int taken = (int) Math.min(stack.getCount(), remaining(root, line));
        if (!simulate && taken > 0) {
            addPaid(root, line, taken);
        }
        return taken;
    }

    /** How much of {@code fluid} the ledger takes, in mB; when not simulating, records it as paid. */
    public static int payFluid(@Nullable CompoundTag root, FluidStack fluid, boolean simulate) {
        if (!hasCost(root) || fluid.isEmpty()) {
            return 0;
        }
        CostLine line = openLine(root, null, fluid.getFluid());
        if (line == null) {
            return 0;
        }
        int taken = (int) Math.min(fluid.getAmount(), remaining(root, line));
        if (!simulate && taken > 0) {
            addPaid(root, line, taken);
        }
        return taken;
    }

    /**
     * Pays from one deposit slot: plain items go in directly; a single fluid container (a bucket, a tank item) is
     * emptied into the ledger and its empty container is left in the slot. A bucket only empties when the ledger
     * still owes at least a full bucket of that fluid. Returns true if anything was paid.
     */
    public static boolean payFromSlot(@Nullable CompoundTag root, IItemHandlerModifiable inventory, int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (!hasCost(root) || stack.isEmpty()) {
            return false;
        }
        int taken = payItems(root, stack, false);
        if (taken > 0) {
            ItemStack rest = stack.copy();
            rest.shrink(taken);
            inventory.setStackInSlot(slot, rest);
            return true;
        }
        if (stack.getCount() != 1) {
            return false;
        }
        FluidActionResult result = FluidUtil.tryEmptyContainer(stack, fluidHandler(() -> root, () -> {
        }), Integer.MAX_VALUE, null, true);
        if (result.isSuccess()) {
            inventory.setStackInSlot(slot, result.getResult());
            return true;
        }
        return false;
    }

    /** Lines that are still owed and resolvable, for automation and display. */
    public static List<CostLine> openLines(@Nullable CompoundTag root) {
        List<CostLine> open = new ArrayList<>();
        if (!hasCost(root)) {
            return open;
        }
        for (CostLine line : lines(root)) {
            boolean resolvable = line.fluid() ? line.fluidType() != null : line.item() != null;
            if (resolvable && remaining(root, line) > 0) {
                open.add(line);
            }
        }
        return open;
    }

    /** Overall progress 0..1 across every line, weighting each line equally. */
    public static float progress(@Nullable CompoundTag root) {
        List<CostLine> lines = lines(root);
        if (lines.isEmpty()) {
            return 0.0F;
        }
        float sum = 0.0F;
        for (CostLine line : lines) {
            sum += line.amount() <= 0 ? 1.0F : Math.min(1.0F, (float) paid(root, line) / line.amount());
        }
        return sum / lines.size();
    }

    /** An insert-only item handler over the ledger: one virtual slot per item line, each accepting only what is owed. */
    public static IItemHandler itemHandler(Supplier<CompoundTag> root, Runnable onChanged) {
        return new IItemHandler() {
            private List<CostLine> itemLines() {
                List<CostLine> out = new ArrayList<>();
                for (CostLine line : lines(root.get())) {
                    if (!line.fluid()) {
                        out.add(line);
                    }
                }
                return out;
            }

            @Override
            public int getSlots() {
                return Math.max(1, this.itemLines().size());
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                CompoundTag tag = root.get();
                int taken = payItems(tag, stack, simulate);
                if (taken <= 0) {
                    return stack;
                }
                if (!simulate) {
                    onChanged.run();
                }
                ItemStack rest = stack.copy();
                rest.shrink(taken);
                return rest;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                List<CostLine> items = this.itemLines();
                CompoundTag tag = root.get();
                if (slot < 0 || slot >= items.size() || tag == null) {
                    return 0;
                }
                return (int) Math.min(Integer.MAX_VALUE, remaining(tag, items.get(slot)));
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return payItems(root.get(), stack, true) > 0;
            }
        };
    }

    /** A fill-only fluid handler over the ledger: one virtual tank per fluid line, sized to what is still owed. */
    public static IFluidHandler fluidHandler(Supplier<CompoundTag> root, Runnable onChanged) {
        return new IFluidHandler() {
            private List<CostLine> fluidLines() {
                List<CostLine> out = new ArrayList<>();
                for (CostLine line : lines(root.get())) {
                    if (line.fluid() && line.fluidType() != null) {
                        out.add(line);
                    }
                }
                return out;
            }

            @Override
            public int getTanks() {
                return Math.max(1, this.fluidLines().size());
            }

            @Override
            public FluidStack getFluidInTank(int tank) {
                return FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                List<CostLine> fluids = this.fluidLines();
                CompoundTag tag = root.get();
                if (tank < 0 || tank >= fluids.size() || tag == null) {
                    return 0;
                }
                return (int) Math.min(Integer.MAX_VALUE, remaining(tag, fluids.get(tank)));
            }

            @Override
            public boolean isFluidValid(int tank, FluidStack stack) {
                return payFluid(root.get(), stack, true) > 0;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                int taken = payFluid(root.get(), resource, action.simulate());
                if (taken > 0 && action.execute()) {
                    onChanged.run();
                }
                return taken;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        };
    }
}

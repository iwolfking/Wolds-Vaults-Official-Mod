package xyz.iwolfking.woldsvaults.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import xyz.iwolfking.woldsvaults.init.ModFluids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The Mobile Extractor Fluid Tank as an item: a portable Prismatic Glue tank that fills from a finished Glue Derrick,
 * pours into anything that accepts held fluid containers, and places down as a pipe-drainable block keeping its glue.
 */
public class MobileFluidTankItem extends BlockItem {
    public static final int CAPACITY = 16000;
    public static final int FILL_LEVELS = 8;
    private static final String FLUID_KEY = "Fluid";

    public MobileFluidTankItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    /** 0 for empty, otherwise 1..{@value #FILL_LEVELS} rounding up, so any glue at all shows. */
    public static int fillLevel(int amount) {
        if (amount <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(FILL_LEVELS, (int) Math.ceil(amount * (double) FILL_LEVELS / CAPACITY)));
    }

    public static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(FLUID_KEY) ? FluidStack.EMPTY : FluidStack.loadFluidStackFromNBT(tag.getCompound(FLUID_KEY));
    }

    public static void setFluid(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty()) {
            if (stack.getTag() != null) {
                stack.getTag().remove(FLUID_KEY);
            }
            return;
        }
        stack.getOrCreateTag().put(FLUID_KEY, fluid.writeToNBT(new CompoundTag()));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return fluid.getFluid() == ModFluids.PRISMATIC_GLUE.get();
            }
        };
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidStack fluid = getFluid(stack);
        if (fluid.isEmpty()) {
            tooltip.add(new TextComponent(String.format("Empty (holds %,d mB of Prismatic Glue)", CAPACITY)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(new TextComponent(String.format("%s: %,d / %,d mB", fluid.getDisplayName().getString(), fluid.getAmount(), CAPACITY)).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(new TextComponent("Right-click a finished Glue Derrick to collect its glue.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(new TextComponent("Place it down to drain it with pipes; it keeps its glue when broken.").withStyle(ChatFormatting.DARK_GRAY));
    }
}

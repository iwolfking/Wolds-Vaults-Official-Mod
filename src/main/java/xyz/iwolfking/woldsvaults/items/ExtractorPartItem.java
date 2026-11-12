package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/** A fabricated extractor part or attachment; carries the rolled {@link PartData} it was forged from. */
public class ExtractorPartItem extends BasicItem {
    private final ExtractorPartType partType;

    public ExtractorPartItem(ResourceLocation id, Properties properties, ExtractorPartType partType) {
        super(id, properties);
        this.partType = partType;
    }

    public ExtractorPartType getPartType() {
        return this.partType;
    }

    public static ItemStack create(ExtractorPartItem item, PartData part) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().put(PartData.TAG, part.write());
        if (part.series != null && ModConfigs.EXTRACTOR_PARTS.getSeries(part.series) != null) {
            stack.getOrCreateTag().putString(ExtractorTooltips.FRAME_TAG, part.series);
        } else {
            WoldLogger.error("Extractor part '{}' has unknown series '{}'; it gets no tooltip frame.", part.name, part.series);
        }
        return stack;
    }

    @Nullable
    public static PartData getPart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : PartData.read(tag.getCompound(PartData.TAG));
    }

    public static boolean isPart(ItemStack stack, ExtractorPartType type) {
        return stack.getItem() instanceof ExtractorPartItem item && item.partType == type && getPart(stack) != null;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        PartData part = getPart(stack);
        if (part == null) {
            return super.getName(stack);
        }
        return new TextComponent(part.name).withStyle(PartData.style(part.rarity));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        PartData part = getPart(stack);
        return part != null && part.maxDurability > 0 && part.durability < part.maxDurability;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        PartData part = getPart(stack);
        return part == null || part.maxDurability <= 0 ? 0 : Math.round(13.0F * part.durabilityFraction());
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        PartData part = getPart(stack);
        return Mth.hsvToRgb((part == null ? 1.0F : part.durabilityFraction()) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        PartData part = getPart(stack);
        if (part == null) {
            tooltip.add(new TextComponent("Blank " + this.partType.getDisplayName().toLowerCase() + " (no part data).").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        ExtractorTooltips.appendPartBody(part, tooltip, ExtractorTooltips.hasShiftDown());
    }
}

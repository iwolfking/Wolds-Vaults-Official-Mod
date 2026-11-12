package xyz.iwolfking.woldsvaults.items;

import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;

import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartRoller;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorQuality;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * A blueprint for one extractor part type. Drops unidentified with a rarity and a quality stamped from the
 * chest opener's item rarity, identifies through the regular gear identification flow into a rolled part
 * description, and is turned into the part itself at the Extractor Forge.
 */
public class ExtractorBlueprintItem extends BasicItem implements IdentifiableItem {
    public static final String TAG = "Blueprint";
    private static final Random RANDOM = new Random();

    private final ExtractorPartType partType;

    public ExtractorBlueprintItem(ResourceLocation id, Properties properties, ExtractorPartType partType) {
        super(id, properties);
        this.partType = partType;
    }

    public ExtractorPartType getPartType() {
        return this.partType;
    }

    public static CompoundTag getData(ItemStack stack) {
        return stack.getOrCreateTag().getCompound(TAG);
    }

    private static void setData(ItemStack stack, CompoundTag data) {
        stack.getOrCreateTag().put(TAG, data);
        stack.getOrCreateTag().putString(ExtractorTooltips.FRAME_TAG, ExtractorTooltips.FRAME_BLUEPRINT);
    }

    /** Creates an unidentified blueprint; quality and rarity may be left for the drop hook to stamp. */
    public static ItemStack create(ExtractorBlueprintItem item, @Nullable VaultGearRarity rarity, @Nullable Float quality) {
        ItemStack stack = new ItemStack(item);
        CompoundTag data = new CompoundTag();
        if (rarity != null) {
            data.putString("Rarity", ExtractorPartsConfig.rarityKey(rarity));
        }
        if (quality != null) {
            data.putFloat("Quality", Math.max(0.0F, quality));
            data.putBoolean("HasQuality", true);
        }
        setData(stack, data);
        return stack;
    }

    public static boolean hasQuality(ItemStack stack) {
        return getData(stack).getBoolean("HasQuality");
    }

    public static float getQuality(ItemStack stack) {
        return getData(stack).getFloat("Quality");
    }

    public static boolean isIdentified(ItemStack stack) {
        return getData(stack).getBoolean("Identified");
    }

    public static VaultGearRarity getBlueprintRarity(ItemStack stack) {
        VaultGearRarity rarity = ExtractorPartsConfig.rarityOf(getData(stack).getString("Rarity"));
        return rarity == null ? VaultGearRarity.SCRAPPY : rarity;
    }

    /** Stamps rarity (if absent) and quality from the given item-rarity points; used at chest-open and by dev commands. */
    public static void stamp(ItemStack stack, double itemRarityPoints, Random random) {
        CompoundTag data = getData(stack);
        if (!data.contains("Rarity")) {
            data.putString("Rarity", ExtractorPartsConfig.rarityKey(ExtractorPartRoller.rollDropRarity(random)));
        }
        VaultGearRarity rarity = getBlueprintRarity(stack);
        data.putFloat("Quality", ExtractorQuality.roll(itemRarityPoints, rarity, random));
        data.putBoolean("HasQuality", true);
        data.putDouble("StampIr", itemRarityPoints);
        setData(stack, data);
    }

    @Nullable
    public static PartData getPart(ItemStack stack) {
        return isIdentified(stack) ? PartData.read(getData(stack).getCompound("Part")) : null;
    }

    /** An identified blueprint of the given slot: it previews like the part it describes but cannot be assembled. */
    public static boolean isIdentifiedBlueprint(ItemStack stack, ExtractorPartType type) {
        return stack.getItem() instanceof ExtractorBlueprintItem item && item.partType == type && getPart(stack) != null;
    }

    /**
     * The blueprint's fabrication ledger, rolling a cost first for blueprints identified before costs existed
     * (logged, since that roll should normally happen at identification). Null for unidentified blueprints.
     */
    @Nullable
    public static CompoundTag ledger(ItemStack stack) {
        PartData part = getPart(stack);
        if (part == null) {
            return null;
        }
        CompoundTag data = stack.getOrCreateTag().getCompound(TAG);
        if (!CostLedger.hasCost(data)) {
            WoldLogger.error("Blueprint {} was identified before fabrication costs existed; rolling its cost now.", part.name);
            CostLedger.setCost(data, ExtractorCosts.roll(part, RANDOM));
            stack.getOrCreateTag().put(TAG, data);
        }
        return data;
    }

    @Override
    public VaultGearState getState(@NotNull ItemStack stack) {
        CompoundTag data = getData(stack);
        if (data.getBoolean("Identified")) {
            return VaultGearState.IDENTIFIED;
        }
        return data.getBoolean("Rolling") ? VaultGearState.ROLLING : VaultGearState.UNIDENTIFIED;
    }

    @Override
    public void setState(@NotNull ItemStack stack, @NotNull VaultGearState state) {
        CompoundTag data = getData(stack);
        data.putBoolean("Rolling", state == VaultGearState.ROLLING);
        if (state == VaultGearState.IDENTIFIED) {
            data.putBoolean("Identified", true);
        }
        setData(stack, data);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (getState(stack) != VaultGearState.UNIDENTIFIED) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.IDENTIFICATION_TOME)) {
            this.instantIdentify(player, stack);
            level.playSound(null, player.blockPosition(), ModSounds.IDENTIFICATION_SFX, SoundSource.PLAYERS, 0.3F, 1.0F);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        this.setState(stack, VaultGearState.ROLLING);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (getState(stack) == VaultGearState.ROLLING) {
            GearRollHelper.tickToll(stack, player, this::tickRoll, finished -> this.tickFinishRoll(finished, player, true));
        }
    }

    @Override
    public void tickRoll(@NotNull ItemStack stack, @Nullable Player player) {
        CompoundTag data = getData(stack);
        VaultGearRarity rarity = getBlueprintRarity(stack);
        String preview;
        if (this.partType == ExtractorPartType.DRILL) {
            List<String> classes = ExtractorPartRoller.eligibleDrillClasses(rarity, data.getFloat("Quality"));
            preview = classes.isEmpty() ? "" : classes.get(RANDOM.nextInt(classes.size()));
        } else if (this.partType == ExtractorPartType.ATTACHMENT) {
            String id = ExtractorPartRoller.rollAttachmentId(RANDOM);
            preview = id == null ? "" : id;
        } else {
            String series = ExtractorPartRoller.rollSeries(rarity, RANDOM);
            preview = series == null ? "" : series;
        }
        data.putString("Preview", preview);
        setData(stack, data);
    }

    @Override
    public void tickFinishRoll(@NotNull ItemStack stack, @Nullable Player player, boolean identify) {
        CompoundTag data = getData(stack);
        if (!data.contains("Rarity")) {
            data.putString("Rarity", ExtractorPartsConfig.rarityKey(ExtractorPartRoller.rollDropRarity(RANDOM)));
        }
        if (!data.getBoolean("HasQuality")) {
            WoldLogger.error("Extractor blueprint {} was identified without a stamped quality (it never went through a chest); rolling quality as if opened at 0 item rarity.", this.getRegistryName());
            data.putFloat("Quality", ExtractorQuality.roll(0.0D, getBlueprintRarity(stack), RANDOM));
            data.putBoolean("HasQuality", true);
        }
        VaultGearRarity rarity = getBlueprintRarity(stack);
        PartData part = ExtractorPartRoller.roll(this.partType, rarity, data.getFloat("Quality"), null, RANDOM);
        if (part == null) {
            WoldLogger.error("Extractor blueprint {} could not roll a part (see errors above); it stays unidentified.", this.getRegistryName());
            data.putBoolean("Rolling", false);
            data.remove("Preview");
            setData(stack, data);
            return;
        }
        data.put("Part", part.write());
        CostLedger.setCost(data, ExtractorCosts.roll(part, RANDOM));
        data.putBoolean("Identified", true);
        data.putBoolean("Rolling", false);
        data.remove("Preview");
        setData(stack, data);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        CompoundTag data = getData(stack);
        VaultGearRarity rarity = getBlueprintRarity(stack);
        if (data.getBoolean("Identified")) {
            PartData part = getPart(stack);
            String name = part == null ? this.partType.getDisplayName() : part.name;
            return new TextComponent("Blueprint: " + name).withStyle(PartData.style(rarity));
        }
        if (data.getBoolean("Rolling") && data.contains("Preview")) {
            return new TextComponent("Blueprint: " + previewName(data.getString("Preview"))).withStyle(PartData.style(rarity));
        }
        return new TextComponent("Unidentified " + this.partType.getDisplayName() + " Blueprint").withStyle(ChatFormatting.GRAY);
    }

    private String previewName(String preview) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        if (this.partType == ExtractorPartType.DRILL) {
            ExtractorPartsConfig.DrillClassEntry entry = cfg.DRILL_CLASSES.get(preview);
            return (entry == null ? preview : entry.name) + " Drill";
        }
        if (this.partType == ExtractorPartType.ATTACHMENT) {
            ExtractorPartsConfig.AttachmentEntry entry = cfg.ATTACHMENTS.get(preview);
            return entry == null ? preview : entry.name;
        }
        ExtractorPartsConfig.SeriesEntry series = cfg.getSeries(preview);
        return series == null ? preview : series.partName(this.partType, series.name + " " + this.partType.getDisplayName());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag data = getData(stack);
        VaultGearRarity rarity = getBlueprintRarity(stack);
        if (!data.getBoolean("Identified")) {
            if (data.getBoolean("HasQuality")) {
                tooltip.add(ExtractorTooltips.quality(data.getFloat("Quality")));
            } else {
                tooltip.add(new TextComponent("Quality: not yet stamped").withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(new TextComponent("Rarity: ").withStyle(ChatFormatting.WHITE)
                    .append(new TextComponent(ExtractorTooltips.rarityName(rarity)).withStyle(PartData.style(rarity))));
            tooltip.add(TextComponent.EMPTY);
            tooltip.add(ExtractorTooltips.hint(data.getBoolean("Rolling") ? "Identifying..." : "Right-click to identify"));
            return;
        }
        PartData part = getPart(stack);
        if (part == null) {
            tooltip.add(new TextComponent("Corrupt blueprint data.").withStyle(ChatFormatting.RED));
            return;
        }
        ExtractorTooltips.appendPartBody(part, tooltip, ExtractorTooltips.hasShiftDown());
        if (CostLedger.hasCost(data)) {
            tooltip.add(TextComponent.EMPTY);
            tooltip.add(new TextComponent("Fabrication cost").withStyle(ChatFormatting.GOLD));
            for (ExtractorCosts.CostLine line : CostLedger.lines(data)) {
                long paid = CostLedger.paid(data, line);
                ChatFormatting color = paid >= line.amount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                tooltip.add(new TextComponent("  ").append(line.displayName().copy().withStyle(ChatFormatting.WHITE))
                        .append(new TextComponent(String.format(": %,d / %,d%s", paid, line.amount(), line.fluid() ? " mB" : "")).withStyle(color)));
            }
        }
        tooltip.add(TextComponent.EMPTY);
        tooltip.add(ExtractorTooltips.hint("Fabricate at an Extraction Forge"));
    }
}

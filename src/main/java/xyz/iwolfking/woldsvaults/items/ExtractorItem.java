package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStats;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import net.minecraft.network.chat.Style;

public class ExtractorItem extends Item {
    public static final String TAG_ROOT = "Extractor";
    private static final int RESET_GRACE_TICKS = 200;

    public ExtractorItem(ResourceLocation id) {
        super(new Properties().tab(ModCreativeTabs.WOLDS_VAULTS).stacksTo(1).fireResistant());
        this.setRegistryName(id);
    }

    public static CompoundTag getData(ItemStack stack) {
        return stack.getOrCreateTag().getCompound(TAG_ROOT);
    }

    private static void setData(ItemStack stack, CompoundTag data) {
        stack.getOrCreateTag().put(TAG_ROOT, data);
    }

    public static boolean isDeployed(ItemStack stack) {
        return getData(stack).getBoolean("Deployed");
    }

    /**
     * Builds a fresh extractor whose stat block, job and durability come straight from the
     * DEFAULT_EXTRACTOR entry of extraction_objective.json.
     */
    public static ItemStack buildDefault(Item item, ExtractionObjectiveConfig config) {
        ExtractionObjectiveConfig.DefaultExtractorEntry cfg = config.DEFAULT_EXTRACTOR;
        ItemStack stack = new ItemStack(item);
        CompoundTag data = new CompoundTag();
        data.putFloat("Health", cfg.health);
        data.putFloat("MaxHealth", cfg.health);
        data.putFloat("Shield", cfg.shield);
        data.putFloat("Armor", cfg.armor);
        data.putFloat("ShieldToughness", cfg.shieldToughness);
        data.putFloat("ShieldRegen", cfg.shieldRegenPerTick);
        data.putInt("Power", cfg.power);
        data.putInt("Speed", cfg.speed);
        data.putFloat("EnergyCapacity", cfg.energyCapacity);
        data.putFloat("EnergyCost", cfg.energyCostPerSecond);
        data.putFloat("EnergyPerCell", cfg.energyPerCell);
        data.putFloat("Quantity", cfg.drillQuantity);
        data.putFloat("Rarity", cfg.drillRarity);
        data.putInt("Durability", cfg.durability);
        data.putInt("MaxDurability", cfg.durability);
        data.putString("Job", cfg.job);
        data.putString("DrillClass", "standard");
        data.putInt("Slots", 1);
        data.putFloat("DurabilityDrainMult", 1.0F);
        data.putFloat("HealthRegen", 0.0F);
        data.putBoolean("Deployed", false);
        setData(stack, data);
        return stack;
    }

    /**
     * Assembles an extractor from rolled parts: the composition formula produces the flat stat block the
     * entity reads, and the parts themselves are kept under {@code Build} for the manage panel and repairs.
     */
    public static ItemStack buildFromParts(Item item, List<PartData> parts, List<PartData> attachments) {
        ItemStack stack = new ItemStack(item);
        CompoundTag data = new CompoundTag();
        data.putBoolean("Deployed", false);
        setData(stack, data);
        rewriteStats(stack, parts, attachments);
        return stack;
    }

    /**
     * Recomposes the flat stat block, the rig variants and the build record from the given parts. Current
     * durability keeps its fraction of the new maximum; deployment tags are left alone.
     */
    public static void rewriteStats(ItemStack stack, List<PartData> parts, List<PartData> attachments) {
        ExtractorStats.Block block = ExtractorStats.compose(parts, attachments);
        CompoundTag data = getData(stack);
        int oldMax = data.getInt("MaxDurability");
        int oldDurability = data.contains("Durability") ? data.getInt("Durability") : oldMax;
        data.putFloat("Health", block.get(ExtractorStat.HEALTH));
        data.putFloat("MaxHealth", block.get(ExtractorStat.HEALTH));
        data.putFloat("Shield", block.get(ExtractorStat.SHIELD));
        data.putFloat("Armor", block.get(ExtractorStat.ARMOR));
        data.putFloat("ShieldToughness", block.get(ExtractorStat.SHIELD_TOUGHNESS));
        data.putFloat("ShieldRegen", block.get(ExtractorStat.SHIELD_REGEN) / 20.0F);
        data.putInt("Power", Math.round(block.get(ExtractorStat.POWER)));
        data.putInt("Speed", Math.round(block.get(ExtractorStat.SPEED)));
        data.putFloat("EnergyCapacity", block.get(ExtractorStat.ENERGY_CAPACITY));
        data.putFloat("EnergyCost", block.get(ExtractorStat.DRAIN));
        data.putFloat("EnergyPerCell", block.get(ExtractorStat.ENERGY_PER_CELL));
        data.putFloat("Quantity", block.get(ExtractorStat.QUANTITY));
        data.putFloat("Rarity", block.get(ExtractorStat.RARITY));
        int durability = Math.round(block.get(ExtractorStat.DURABILITY));
        int current = oldMax <= 0 ? durability : (int) Math.round((double) durability * Math.max(0, oldDurability) / oldMax);
        data.putInt("Durability", Math.max(0, Math.min(durability, current)));
        data.putInt("MaxDurability", durability);
        data.putFloat("DurabilityDrainMult", block.get(ExtractorStat.DURABILITY_DRAIN_MULT));
        data.putFloat("HealthRegen", block.get(ExtractorStat.HEALTH_REGEN) / 20.0F);
        data.putString("DrillClass", block.drillClass);
        String job = ExtractorStats.jobFor(block.drillClass);
        if (job == null) {
            WoldLogger.error("Assembled extractor has drill class '{}' with no job in DRILL_CLASSES; it will not deploy until the config names one.", block.drillClass);
            job = "";
        }
        data.putString("Job", job);
        data.putInt("Slots", block.slots);
        data.putString("Variants", String.join(",", block.variantBones));
        data.putString("FrameSeries", block.frameSeries);
        CompoundTag build = new CompoundTag();
        ListTag partList = new ListTag();
        for (PartData part : parts) {
            partList.add(part.write());
        }
        ListTag attachmentList = new ListTag();
        for (PartData attachment : attachments) {
            attachmentList.add(attachment.write());
        }
        build.put("Parts", partList);
        build.put("Attachments", attachmentList);
        data.put("Build", build);
        setData(stack, data);
        if (block.frameSeries.isEmpty() || ModConfigs.EXTRACTOR_PARTS.getSeries(block.frameSeries) == null) {
            WoldLogger.error("Assembled extractor has no recognisable series ('{}'); it gets no tooltip frame.", block.frameSeries);
            stack.getOrCreateTag().remove(ExtractorTooltips.FRAME_TAG);
        } else {
            stack.getOrCreateTag().putString(ExtractorTooltips.FRAME_TAG, block.frameSeries);
        }
    }

    public static List<PartData> getBuildParts(ItemStack stack) {
        return readPartList(getData(stack).getCompound("Build").getList("Parts", Tag.TAG_COMPOUND));
    }

    public static List<PartData> getBuildAttachments(ItemStack stack) {
        return readPartList(getData(stack).getCompound("Build").getList("Attachments", Tag.TAG_COMPOUND));
    }

    private static List<PartData> readPartList(ListTag list) {
        List<PartData> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            PartData part = PartData.read(list.getCompound(i));
            if (part != null) {
                parts.add(part);
            }
        }
        return parts;
    }

    public static boolean isAssembled(ItemStack stack) {
        return stack.getItem() instanceof ExtractorItem && getData(stack).contains("Build");
    }

    public static int getSlots(ItemStack stack) {
        return getData(stack).getInt("Slots");
    }

    public static String getFrameSeries(ItemStack stack) {
        CompoundTag data = getData(stack);
        if (data.contains("FrameSeries")) {
            return data.getString("FrameSeries");
        }
        return isAssembled(stack) ? ExtractorStats.frameSeries(getBuildParts(stack)) : "";
    }

    public static String getVariants(ItemStack stack) {
        return getData(stack).getString("Variants");
    }

    public static String getDrillClass(ItemStack stack) {
        return getData(stack).getString("DrillClass");
    }

    public static void setDurability(ItemStack stack, int durability) {
        CompoundTag data = getData(stack);
        data.putInt("Durability", Math.max(0, Math.min(durability, data.getInt("MaxDurability"))));
        setData(stack, data);
    }

    public static void markDeployed(ItemStack stack, UUID vaultId, UUID entityId) {
        CompoundTag data = getData(stack);
        data.putBoolean("Deployed", true);
        data.putUUID("DeployVault", vaultId);
        data.putUUID("DeployEntity", entityId);
        data.putInt("ResetCounter", 0);
        setData(stack, data);
    }

    /**
     * Writes session results back onto the item and returns it to the armed state. Health always comes
     * back full: health and shields restore on every vault exit; the hull's durability and every
     * attachment's own wear pool carry over. An attachment that ran dry during the session stops
     * contributing when the stat block is recomposed here.
     */
    public static void armWithResults(ItemStack stack, int durability, float hp, @Nullable float[] attachmentDurability) {
        if (attachmentDurability != null) {
            writeAttachmentDurability(stack, attachmentDurability);
        }
        CompoundTag data = getData(stack);
        data.putInt("Durability", Math.max(0, durability));
        data.putFloat("Health", data.getFloat("MaxHealth"));
        clearDeployment(data);
        setData(stack, data);
    }

    /** Stores the session's attachment wear and recomposes, so a spent attachment drops out of the stat block. */
    private static void writeAttachmentDurability(ItemStack stack, float[] attachmentDurability) {
        if (!isAssembled(stack)) {
            return;
        }
        List<PartData> attachments = getBuildAttachments(stack);
        if (attachments.size() != attachmentDurability.length) {
            WoldLogger.error("Extractor came back with {} attachment wear values but {} attachments on the item; only the matching ones carried over.",
                    attachmentDurability.length, attachments.size());
        }
        for (int i = 0; i < Math.min(attachments.size(), attachmentDurability.length); i++) {
            PartData attachment = attachments.get(i);
            attachment.durability = Math.max(0, Math.min(attachment.maxDurability, Math.round(attachmentDurability[i])));
        }
        rewriteStats(stack, getBuildParts(stack), attachments);
    }

    private static void clearDeployment(CompoundTag data) {
        data.putBoolean("Deployed", false);
        data.remove("DeployVault");
        data.remove("DeployEntity");
        data.remove("ResetCounter");
    }

    @Nullable
    public static UUID getDeployedEntity(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data.hasUUID("DeployEntity") ? data.getUUID("DeployEntity") : null;
    }

    /** This extractor's identity for keying an extraction room, or null before it ever placed a thumper. */
    @Nullable
    public static UUID getRigId(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data.hasUUID("RigId") ? data.getUUID("RigId") : null;
    }

    /** Returns this extractor's identity, giving it a fresh one first if it has none. */
    public static UUID ensureRigId(ItemStack stack) {
        CompoundTag data = getData(stack);
        if (!data.hasUUID("RigId")) {
            data.putUUID("RigId", UUID.randomUUID());
            setData(stack, data);
        }
        return getData(stack).getUUID("RigId");
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data.getInt("MaxDurability") > 0 && data.getInt("Durability") < data.getInt("MaxDurability");
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag data = getData(stack);
        int max = data.getInt("MaxDurability");
        return max <= 0 ? 0 : Math.round(13.0F * data.getInt("Durability") / max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        CompoundTag data = getData(stack);
        int max = data.getInt("MaxDurability");
        float fraction = max <= 0 ? 0.0F : Math.max(0.0F, (float) data.getInt("Durability") / max);
        return Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (isDeployed(stack)) {
            player.displayClientMessage(new TextComponent("This extractor is already deployed.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (getData(stack).getInt("Durability") <= 0) {
            player.displayClientMessage(new TextComponent("This extractor is out of durability and cannot drill.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Optional<Vault> vaultOpt = VaultUtils.getVault(level);
        if (vaultOpt.isEmpty()) {
            player.displayClientMessage(new TextComponent("Extractors only work inside extraction vaults.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Optional<ExtractionObjective> objective = ExtractionObjective.get(vaultOpt.get());
        if (objective.isEmpty()) {
            player.displayClientMessage(new TextComponent("This vault has no extraction sites.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        return objective.get().tryDeploy(vaultOpt.get(), player, stack, context.getClickedPos());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level.isClientSide() || !isDeployed(stack) || level.getGameTime() % 20L != 0L) {
            return;
        }
        CompoundTag data = getData(stack);
        UUID deployVault = data.hasUUID("DeployVault") ? data.getUUID("DeployVault") : null;
        Optional<Vault> vault = VaultUtils.getVault(holder.level);
        boolean insideDeployVault = deployVault != null && vault.isPresent() && deployVault.equals(vault.get().get(Vault.ID));
        if (insideDeployVault) {
            data.putInt("ResetCounter", 0);
            setData(stack, data);
            return;
        }
        int counter = data.getInt("ResetCounter") + 20;
        if (counter < RESET_GRACE_TICKS) {
            data.putInt("ResetCounter", counter);
            setData(stack, data);
            return;
        }
        clearDeployment(data);
        setData(stack, data);
        WoldLogger.info("Extractor item held by {} re-armed itself: its deployment vault {} is gone or was left behind.",
                holder.getName().getString(), deployVault);
        if (holder instanceof Player player) {
            player.displayClientMessage(new TextComponent("Your extractor re-armed itself.").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("display")) {
            return super.getName(stack);
        }
        if (!isAssembled(stack)) {
            return super.getName(stack);
        }
        String series = getFrameSeries(stack);
        return new TextComponent(ExtractorTooltips.extractorName(series)).withStyle(Style.EMPTY.withColor(ExtractorTooltips.seriesColor(series)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag data = getData(stack);
        if (!data.contains("MaxHealth")) {
            tooltip.add(new TextComponent("Uninitialized extractor.").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        if (data.getBoolean("Deployed")) {
            tooltip.add(new TextComponent("DEPLOYED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        String drillClass = data.getString("DrillClass");
        tooltip.add(ExtractorTooltips.labelValue("Drill: ", ExtractorTooltips.drillClassName(drillClass), ExtractorTooltips.DRILL_CLASS_COLOR)
                .append(ExtractorTooltips.labelValue("   Job: ", ExtractorTooltips.jobName(drillClass), ExtractorTooltips.SLOT_COLOR)));
        tooltip.add(ExtractorTooltips.labelValue("Durability: ", String.format("%,d/%,d", data.getInt("Durability"), data.getInt("MaxDurability")), ExtractorStat.DURABILITY.getColor()));
        if (!isAssembled(stack)) {
            tooltip.add(TextComponent.EMPTY);
            ExtractorTooltips.appendGroupedStats(blockFromData(data), tooltip);
            tooltip.add(new TextComponent("Dev extractor (no build record).").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        List<PartData> parts = getBuildParts(stack);
        List<PartData> attachments = getBuildAttachments(stack);
        int slots = data.getInt("Slots");
        StringBuilder pips = new StringBuilder();
        for (int i = 0; i < slots; i++) {
            pips.append(i > 0 ? " " : "").append(i < attachments.size() ? "⬢" : "⬡");
        }
        tooltip.add(ExtractorTooltips.labelValue("Attachments: ", slots == 0 ? "None" : pips.toString(), slots == 0 ? 0x555555 : 0xFFFFFF));
        tooltip.add(TextComponent.EMPTY);
        ExtractorTooltips.appendGroupedStats(ExtractorStats.compose(parts, attachments), tooltip);
        if (!attachments.isEmpty()) {
            tooltip.add(TextComponent.EMPTY);
            tooltip.add(new TextComponent("Attachments:").withStyle(ChatFormatting.GRAY));
            for (PartData attachment : attachments) {
                tooltip.add(new TextComponent("  " + attachment.name).withStyle(PartData.style(attachment.rarity))
                        .append(ExtractorTooltips.attachmentCharge(attachment)));
            }
        }
        tooltip.add(TextComponent.EMPTY);
        if (ExtractorTooltips.hasShiftDown()) {
            tooltip.add(new TextComponent("Parts:").withStyle(ChatFormatting.GRAY));
            for (PartData part : parts) {
                tooltip.add(new TextComponent("  ").append(ExtractorTooltips.partSummary(part)));
            }
        } else {
            tooltip.add(ExtractorTooltips.hint("Hold Shift to view parts"));
        }
    }

    /** The flat stat block of an extractor with no build record, read straight from its NBT. */
    private static ExtractorStats.Block blockFromData(CompoundTag data) {
        ExtractorStats.Block block = new ExtractorStats.Block();
        block.values.put(ExtractorStat.POWER, (float) data.getInt("Power"));
        block.values.put(ExtractorStat.SPEED, (float) data.getInt("Speed"));
        block.values.put(ExtractorStat.HEALTH, data.getFloat("MaxHealth"));
        block.values.put(ExtractorStat.ARMOR, data.getFloat("Armor"));
        block.values.put(ExtractorStat.SHIELD, data.getFloat("Shield"));
        block.values.put(ExtractorStat.SHIELD_TOUGHNESS, data.getFloat("ShieldToughness"));
        block.values.put(ExtractorStat.SHIELD_REGEN, data.getFloat("ShieldRegen") * 20.0F);
        block.values.put(ExtractorStat.ENERGY_CAPACITY, data.getFloat("EnergyCapacity"));
        block.values.put(ExtractorStat.DRAIN, data.getFloat("EnergyCost"));
        block.values.put(ExtractorStat.ENERGY_PER_CELL, data.getFloat("EnergyPerCell"));
        block.values.put(ExtractorStat.QUANTITY, data.getFloat("Quantity"));
        block.values.put(ExtractorStat.RARITY, data.getFloat("Rarity"));
        block.values.put(ExtractorStat.HEALTH_REGEN, data.getFloat("HealthRegen") * 20.0F);
        block.values.put(ExtractorStat.DURABILITY_DRAIN_MULT, data.contains("DurabilityDrainMult") ? data.getFloat("DurabilityDrainMult") : 1.0F);
        block.slots = data.getInt("Slots");
        block.drillClass = data.getString("DrillClass");
        return block;
    }
}

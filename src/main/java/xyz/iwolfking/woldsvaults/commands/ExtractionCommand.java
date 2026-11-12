package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.extraction.jobs.GreedCardUpgrades;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartRoller;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorQuality;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;
import xyz.iwolfking.woldsvaults.objectives.ExtractionCrystalObjective;

import net.minecraft.ChatFormatting;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import net.minecraft.nbt.CompoundTag;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.items.FabricationPatternItem;

public class ExtractionCommand {
    private static final Random RANDOM = new Random();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvextraction")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("extractor")
                        .then(Commands.argument("itemRarity", IntegerArgumentType.integer(0, 100000)).suggests(IR_ANCHORS)
                                .then(Commands.argument("series", StringArgumentType.word()).suggests(SERIES)
                                        .then(Commands.argument("drill", StringArgumentType.word()).suggests(DRILL_CLASSES)
                                                .executes(context -> giveExtractor(context, IntegerArgumentType.getInteger(context, "itemRarity"), StringArgumentType.getString(context, "series"), StringArgumentType.getString(context, "drill"), null))
                                                .then(Commands.argument("rarity", StringArgumentType.word()).suggests(RARITIES)
                                                        .executes(context -> giveExtractor(context, IntegerArgumentType.getInteger(context, "itemRarity"), StringArgumentType.getString(context, "series"), StringArgumentType.getString(context, "drill"), StringArgumentType.getString(context, "rarity"))))))))
                .then(Commands.literal("cells")
                        .executes(context -> giveCells(context.getSource(), 16))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 1024))
                                .executes(context -> giveCells(context.getSource(), IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.literal("crystal")
                        .executes(context -> giveCrystal(context.getSource(), 100))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 1000))
                                .executes(context -> giveCrystal(context.getSource(), IntegerArgumentType.getInteger(context, "level")))))
                .then(Commands.literal("greedcard")
                        .then(Commands.argument("class", StringArgumentType.word()).suggests(GREED_CLASSES)
                                .executes(context -> giveGreedCard(context.getSource(), StringArgumentType.getString(context, "class")))))
                .then(Commands.literal("blueprint")
                        .then(Commands.argument("part", StringArgumentType.word()).suggests(PART_TYPES)
                                .executes(context -> giveBlueprint(context, null, null))
                                .then(Commands.argument("ir", IntegerArgumentType.integer(0, 100000)).suggests(IR_ANCHORS)
                                        .executes(context -> giveBlueprint(context, IntegerArgumentType.getInteger(context, "ir"), null))
                                        .then(Commands.argument("rarity", StringArgumentType.word()).suggests(RARITIES)
                                                .executes(context -> giveBlueprint(context, IntegerArgumentType.getInteger(context, "ir"), StringArgumentType.getString(context, "rarity")))))))
                .then(Commands.literal("part")
                        .then(Commands.argument("part", StringArgumentType.word()).suggests(PART_TYPES)
                                .then(Commands.argument("ir", IntegerArgumentType.integer(0, 100000)).suggests(IR_ANCHORS)
                                        .then(Commands.argument("rarity", StringArgumentType.word()).suggests(RARITIES)
                                                .executes(context -> givePart(context, null))
                                                .then(Commands.argument("series", StringArgumentType.word()).suggests(PRESETS_FOR_PART)
                                                        .executes(context -> givePart(context, StringArgumentType.getString(context, "series"))))))))
                .then(Commands.literal("identify").executes(context -> identifyHeld(context.getSource())))
                .then(Commands.literal("forge").executes(context -> forgeHeld(context.getSource())))
                .then(Commands.literal("pattern")
                        .then(Commands.argument("component", StringArgumentType.word()).suggests(COMPONENTS)
                                .executes(context -> givePattern(context.getSource(), StringArgumentType.getString(context, "component")))))
                .then(Commands.literal("paycost").executes(context -> payHeld(context.getSource())))
                .then(Commands.literal("cost").executes(context -> showHeldCost(context.getSource()))));
    }

    private static final SuggestionProvider<CommandSourceStack> PART_TYPES = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(ExtractorPartType.values()).map(ExtractorPartType::getId), builder);
    private static final SuggestionProvider<CommandSourceStack> JOB_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(ModConfigs.EXTRACTION_OBJECTIVE.JOBS.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> GREED_CLASSES = (context, builder) ->
            SharedSuggestionProvider.suggest(ModConfigs.EXTRACTION_OBJECTIVE.GREED_INFUSER.classOrder, builder);
    private static final SuggestionProvider<CommandSourceStack> RARITIES = (context, builder) ->
            SharedSuggestionProvider.suggest(List.of("scrappy", "common", "rare", "epic", "omega"), builder);
    private static final SuggestionProvider<CommandSourceStack> IR_ANCHORS = (context, builder) ->
            SharedSuggestionProvider.suggest(List.of("0", "100", "450", "1800", "4000", "10000"), builder);
    private static final SuggestionProvider<CommandSourceStack> SERIES = (context, builder) ->
            SharedSuggestionProvider.suggest(ModConfigs.EXTRACTOR_PARTS.SERIES.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> DRILL_CLASSES = (context, builder) ->
            SharedSuggestionProvider.suggest(ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.keySet(), builder);
    /** For a drill the preset is a drill class, for an attachment its id, for everything else a series. */
    private static final SuggestionProvider<CommandSourceStack> PRESETS_FOR_PART = (context, builder) -> {
        ExtractorPartType type;
        try {
            type = ExtractorPartType.byId(StringArgumentType.getString(context, "part"));
        } catch (IllegalArgumentException missing) {
            type = null;
        }
        Iterable<String> options;
        if (type == ExtractorPartType.DRILL) {
            options = ExtractorPartRoller.rollableDrillClasses(VaultGearRarity.OMEGA);
        } else if (type == ExtractorPartType.ATTACHMENT) {
            options = ModConfigs.EXTRACTOR_PARTS.ATTACHMENTS.keySet();
        } else {
            options = ModConfigs.EXTRACTOR_PARTS.SERIES.keySet();
        }
        return SharedSuggestionProvider.suggest(options, builder);
    };

    /** Dev shortcut for the Extractor Forge: identifies the held blueprint if needed and turns it into its part, no costs. */
    private static final SuggestionProvider<CommandSourceStack> COMPONENTS = (context, builder) ->
            SharedSuggestionProvider.suggest(ModConfigs.EXTRACTOR_COSTS.COMPONENTS.keySet(), builder);

    private static int givePattern(CommandSourceStack source, String component) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (ModConfigs.EXTRACTOR_COSTS.componentFor(component) == null) {
            source.sendFailure(new TextComponent("Unknown component '" + component + "'. Known: " + String.join(", ", ModConfigs.EXTRACTOR_COSTS.COMPONENTS.keySet())));
            return 0;
        }
        player.getInventory().placeItemBackInInventory(FabricationPatternItem.encode(component));
        source.sendSuccess(new TextComponent("Gave a Fabrication Pattern for " + component + "."), false);
        return 1;
    }

    /** Dev: marks every line of the held identified blueprint's fabrication cost as paid. */
    private static int payHeld(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        CompoundTag ledger = held.getItem() instanceof ExtractorBlueprintItem ? ExtractorBlueprintItem.ledger(held) : null;
        if (ledger == null) {
            source.sendFailure(new TextComponent("Hold an identified extractor blueprint in your main hand."));
            return 0;
        }
        CompoundTag paid = new CompoundTag();
        for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
            paid.putLong(line.key(), line.amount());
        }
        ledger.put(CostLedger.PAID, paid);
        source.sendSuccess(new TextComponent("Paid the held blueprint's fabrication cost (dev)."), false);
        return 1;
    }

    /** Prints the held blueprint's fabrication cost lines and how much of each is paid. */
    private static int showHeldCost(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        CompoundTag ledger = held.getItem() instanceof ExtractorBlueprintItem ? ExtractorBlueprintItem.ledger(held) : null;
        if (ledger == null) {
            source.sendFailure(new TextComponent("Hold an identified extractor blueprint in your main hand."));
            return 0;
        }
        for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
            long paid = CostLedger.paid(ledger, line);
            source.sendSuccess(new TextComponent(String.format("%s: %,d / %,d%s", line.displayName().getString(), paid, line.amount(), line.fluid() ? " mB" : "")), false);
        }
        return 1;
    }

    private static int forgeHeld(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ExtractorBlueprintItem blueprint)) {
            source.sendFailure(new TextComponent("Hold an extractor blueprint in your main hand."));
            return 0;
        }
        if (!ExtractorBlueprintItem.isIdentified(held)) {
            blueprint.tickFinishRoll(held, player, true);
        }
        PartData part = ExtractorBlueprintItem.getPart(held);
        if (part == null) {
            source.sendFailure(new TextComponent("The blueprint could not be identified; see the server log."));
            return 0;
        }
        ExtractorPartItem partItem = ModItems.EXTRACTOR_PARTS.get(blueprint.getPartType());
        ItemStack result = ExtractorPartItem.create(partItem, part);
        player.getInventory().setItem(player.getInventory().selected, result);
        source.sendSuccess(new TextComponent("Forged " + part.name + " [" + part.describeOrigin() + "] from the held blueprint (dev: no cost)."), false);
        return 1;
    }

    /**
     * A fully crafted extractor: the six structural parts rolled in {@code series} plus the named drill class, every
     * part at the given item-rarity points. The part rarity defaults to the lowest one the series is sold at; the
     * drill is forced even when the rolled quality is below its {@code minQuality} gate, which is reported.
     */
    private static int giveExtractor(CommandContext<CommandSourceStack> context, int ir, String series, String drill, @Nullable String rarityKey) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        if (ModConfigs.EXTRACTOR_PARTS.getSeries(series) == null) {
            source.sendFailure(new TextComponent("Unknown series '" + series + "'. Known: " + String.join(", ", ModConfigs.EXTRACTOR_PARTS.SERIES.keySet())));
            return 0;
        }
        ExtractorPartsConfig.DrillClassEntry drillClass = ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.get(drill);
        if (drillClass == null) {
            source.sendFailure(new TextComponent("Unknown drill class '" + drill + "'. Known: " + String.join(", ", ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.keySet())));
            return 0;
        }
        VaultGearRarity rarity = rarityKey == null ? lowestRarityFor(series) : rarityArg(source, rarityKey);
        if (rarity == null) {
            return 0;
        }
        List<PartData> parts = new ArrayList<>();
        float drillQuality = 0.0F;
        for (ExtractorPartType type : ExtractorPartType.CORE_PARTS) {
            float quality = ExtractorQuality.roll(ir, rarity, RANDOM);
            boolean isDrill = type == ExtractorPartType.DRILL;
            PartData part = ExtractorPartRoller.roll(type, rarity, quality, isDrill ? drill : series, RANDOM);
            if (part == null) {
                source.sendFailure(new TextComponent("Could not roll a " + type.getDisplayName() + "; see the server log."));
                return 0;
            }
            if (isDrill) {
                drillQuality = quality;
            }
            parts.add(part);
        }
        ItemStack stack = ExtractorItem.buildFromParts(ModItems.EXTRACTOR, parts, new ArrayList<>());
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        String rarityName = ExtractorPartsConfig.rarityKey(rarity);
        source.sendSuccess(new TextComponent("Crafted a " + rarityName + " " + ModConfigs.EXTRACTOR_PARTS.getSeries(series).name + " extractor with a " + drillClass.name + " drill at IR " + ir + "."), false);
        float gate = ExtractorPartRoller.drillMinQuality(drill);
        if (drillQuality < gate) {
            source.sendSuccess(new TextComponent(String.format("Note: the drill rolled quality %.2f, below the %s minQuality of %.2f; it was forced on anyway.", drillQuality, drillClass.name, gate)).withStyle(ChatFormatting.YELLOW), false);
        }
        return 1;
    }

    /** The lowest rarity whose series list contains the series, or scrappy (logged) if no rarity lists it. */
    private static VaultGearRarity lowestRarityFor(String series) {
        for (VaultGearRarity rarity : VaultGearRarity.values()) {
            if (ModConfigs.EXTRACTOR_PARTS.getSeriesFor(rarity).contains(series)) {
                return rarity;
            }
        }
        WoldLogger.error("Series '{}' is listed under no rarity in extractor_parts.json SERIES_BY_RARITY; the command falls back to scrappy.", series);
        return VaultGearRarity.SCRAPPY;
    }

    private static int giveGreedCard(CommandSourceStack source, String cls) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack card = GreedCardUpgrades.createCard(cls, RANDOM);
        if (card.isEmpty()) {
            source.sendFailure(new TextComponent("No greed card pool for class '" + cls + "'; see the server log."));
            return 0;
        }
        ItemHandlerHelper.giveItemToPlayer(player, card);
        source.sendSuccess(new TextComponent("Gave a tier 1 " + GreedCardUpgrades.displayName(cls) + " Greed card."), false);
        return 1;
    }

    private static int giveCells(CommandSourceStack source, int count) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.POWER_CELL, count));
        source.sendSuccess(new TextComponent("Gave " + count + " power cells."), false);
        return 1;
    }

    private static int giveCrystal(CommandSourceStack source, int level) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack crystal = new ItemStack(iskallia.vault.init.ModItems.VAULT_CRYSTAL);
        CrystalData data = CrystalData.read(crystal);
        data.getProperties().setLevel(level);
        data.setObjective(new ExtractionCrystalObjective());
        data.getProperties().setUnmodifiable(true);
        data.write(crystal);
        ItemHandlerHelper.giveItemToPlayer(player, crystal);
        source.sendSuccess(new TextComponent("Gave a level " + level + " extraction vault crystal."), false);
        return 1;
    }

    private static ExtractorPartType partArg(CommandContext<CommandSourceStack> context) {
        ExtractorPartType type = ExtractorPartType.byId(StringArgumentType.getString(context, "part"));
        if (type == null) {
            context.getSource().sendFailure(new TextComponent("Unknown part type. Use one of: chassis, power_core, drill, legs, shield_array, radiator, scanner, attachment."));
        }
        return type;
    }

    private static VaultGearRarity rarityArg(CommandSourceStack source, String key) {
        VaultGearRarity rarity = ExtractorPartsConfig.rarityOf(key);
        if (rarity == null || rarity == VaultGearRarity.UNIQUE) {
            source.sendFailure(new TextComponent("Unknown rarity '" + key + "'. Use scrappy, common, rare, epic or omega."));
            return null;
        }
        return rarity;
    }

    private static int giveBlueprint(CommandContext<CommandSourceStack> context, Integer ir, String rarityKey) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ExtractorPartType type = partArg(context);
        if (type == null) {
            return 0;
        }
        VaultGearRarity rarity = rarityKey == null ? null : rarityArg(context.getSource(), rarityKey);
        if (rarityKey != null && rarity == null) {
            return 0;
        }
        ItemStack stack = ExtractorBlueprintItem.create(ModItems.EXTRACTOR_BLUEPRINTS.get(type), rarity, null);
        if (ir != null) {
            ExtractorBlueprintItem.stamp(stack, ir, RANDOM);
        }
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        context.getSource().sendSuccess(new TextComponent(ir == null
                ? "Gave an unstamped " + type.getDisplayName() + " blueprint (a chest opening would stamp it; identifying now rolls at IR 0)."
                : "Gave a " + ExtractorBlueprintItem.getBlueprintRarity(stack) + " " + type.getDisplayName() + " blueprint stamped at IR " + ir
                + String.format(" -> Quality %.2f.", ExtractorBlueprintItem.getQuality(stack))), false);
        return 1;
    }

    private static int givePart(CommandContext<CommandSourceStack> context, String series) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ExtractorPartType type = partArg(context);
        if (type == null) {
            return 0;
        }
        VaultGearRarity rarity = rarityArg(context.getSource(), StringArgumentType.getString(context, "rarity"));
        if (rarity == null) {
            return 0;
        }
        int ir = IntegerArgumentType.getInteger(context, "ir");
        float quality = ExtractorQuality.roll(ir, rarity, RANDOM);
        PartData part = ExtractorPartRoller.roll(type, rarity, quality, series, RANDOM);
        if (part == null) {
            context.getSource().sendFailure(new TextComponent("The part could not be rolled; see the server log."));
            return 0;
        }
        ItemHandlerHelper.giveItemToPlayer(player, ExtractorPartItem.create(ModItems.EXTRACTOR_PARTS.get(type), part));
        context.getSource().sendSuccess(new TextComponent("Gave " + part.name + " [" + part.describeOrigin() + "]."), false);
        return 1;
    }

    private static int identifyHeld(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ExtractorBlueprintItem blueprint)) {
            source.sendFailure(new TextComponent("Hold an extractor blueprint in your main hand."));
            return 0;
        }
        if (ExtractorBlueprintItem.isIdentified(held)) {
            source.sendFailure(new TextComponent("That blueprint is already identified."));
            return 0;
        }
        blueprint.tickFinishRoll(held, player, true);
        source.sendSuccess(new TextComponent("Identified: " + held.getHoverName().getString()), false);
        return 1;
    }
}

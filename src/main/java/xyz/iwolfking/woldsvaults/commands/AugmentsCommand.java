package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.SpellPlan;
import xyz.iwolfking.woldsvaults.augments.item.AugmentItemData;
import xyz.iwolfking.woldsvaults.augments.item.AugmentRoller;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * {@code /wvaugments}: arranges the augments on the off-hand wand or focus into per-ability recipes until the
 * augment screen exists. Every recipe is validated on the server against the table and the item's tokens. The
 * {@code grant} and {@code reroll} subcommands are operator-only test tools.
 */
public final class AugmentsCommand {
    private static final SuggestionProvider<CommandSourceStack> ABILITIES = (context, builder) -> {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggest(AugmentAbilities.all(config).values().stream()
                .filter(AugmentAbility::isCastable).map(AugmentAbility::id), builder);
    };
    private static final SuggestionProvider<CommandSourceStack> AUGMENTS = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(AugmentType.values()).map(AugmentType::id), builder);

    private AugmentsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvaugments")
                .then(Commands.literal("show").executes(context -> show(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("ability", StringArgumentType.word()).suggests(ABILITIES)
                                .then(Commands.argument("glyphs", StringArgumentType.greedyString())
                                        .executes(AugmentsCommand::set))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("ability", StringArgumentType.word()).suggests(ABILITIES)
                                .executes(context -> clear(context.getSource(), StringArgumentType.getString(context, "ability")))))
                .then(Commands.literal("grant").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("augment", StringArgumentType.word()).suggests(AUGMENTS)
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 16))
                                        .executes(context -> grant(context.getSource(), StringArgumentType.getString(context, "augment"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("reroll").requires(source -> source.hasPermission(2))
                        .executes(context -> reroll(context.getSource()))));
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = heldItem(source, player);
        if (stack == null) {
            return 0;
        }
        Map<AugmentType, Integer> rolled = AugmentItemData.rolled(stack);
        source.sendSuccess(new TextComponent("Augments: " + (rolled.isEmpty() ? "none" : formatTokens(rolled))).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        Map<String, List<String>> recipes = AugmentItemData.recipes(stack);
        if (recipes.isEmpty()) {
            source.sendSuccess(new TextComponent("No recipes. Use /wvaugments set <ability> <glyphs...>").withStyle(ChatFormatting.GRAY), false);
        }
        recipes.forEach((ability, glyphs) -> source.sendSuccess(new TextComponent("  " + AugmentAbilities.displayName(ability)
                + ": " + String.join(" ", glyphs)).withStyle(ChatFormatting.GRAY), false));
        FocusAugmentsConfig config = FocusAugments.config();
        if (config != null && FocusAugments.sourceItem(player).isEmpty()) {
            source.sendSuccess(new TextComponent("This item is not active (not identified, broken, or above your level).").withStyle(ChatFormatting.RED), false);
        }
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        FocusAugmentsConfig config = requireConfig(source);
        ItemStack stack = heldItem(source, player);
        if (config == null || stack == null) {
            return 0;
        }
        String ability = StringArgumentType.getString(context, "ability");
        List<String> glyphs = new ArrayList<>();
        for (String glyph : StringArgumentType.getString(context, "glyphs").trim().split("\\s+")) {
            if (!glyph.isEmpty()) {
                AugmentType type = AugmentType.byId(glyph);
                glyphs.add(type != null ? type.id() : glyph);
            }
        }
        SpellPlan plan = SpellPlan.parse(config, ability, glyphs);
        if (!plan.isValid()) {
            source.sendFailure(new TextComponent("Invalid recipe: " + String.join("; ", plan.errors())));
            return 0;
        }
        Map<String, List<String>> recipes = new LinkedHashMap<>(AugmentItemData.recipes(stack));
        recipes.put(ability, glyphs);
        String budget = AugmentItemData.checkBudget(config, stack, recipes);
        if (budget != null) {
            source.sendFailure(new TextComponent("Can't store the recipe: " + budget));
            return 0;
        }
        AugmentItemData.setRecipe(stack, ability, glyphs);
        source.sendSuccess(new TextComponent("Recipe on " + AugmentAbilities.displayName(ability) + ": " + String.join(" ", glyphs)
                + " (" + plan.casts() + " casts per press)").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private static int clear(CommandSourceStack source, String ability) throws CommandSyntaxException {
        ItemStack stack = heldItem(source, source.getPlayerOrException());
        if (stack == null) {
            return 0;
        }
        if (AugmentItemData.recipe(stack, ability) == null) {
            source.sendFailure(new TextComponent("No recipe on " + ability));
            return 0;
        }
        AugmentItemData.setRecipe(stack, ability, null);
        source.sendSuccess(new TextComponent("Cleared the recipe on " + AugmentAbilities.displayName(ability)), false);
        return 1;
    }

    private static int grant(CommandSourceStack source, String augment, int count) throws CommandSyntaxException {
        ItemStack stack = heldItem(source, source.getPlayerOrException());
        AugmentType type = AugmentType.byId(augment);
        if (stack == null) {
            return 0;
        }
        if (type == null) {
            source.sendFailure(new TextComponent("Unknown augment " + augment));
            return 0;
        }
        Map<AugmentType, Integer> rolled = new EnumMap<>(AugmentType.class);
        rolled.putAll(AugmentItemData.rolled(stack));
        rolled.merge(type, count, Integer::sum);
        AugmentItemData.setRolled(stack, rolled);
        source.sendSuccess(new TextComponent("Augments: " + formatTokens(rolled)), true);
        return 1;
    }

    private static int reroll(CommandSourceStack source) throws CommandSyntaxException {
        FocusAugmentsConfig config = requireConfig(source);
        ItemStack stack = heldItem(source, source.getPlayerOrException());
        if (config == null || stack == null) {
            return 0;
        }
        Map<AugmentType, Integer> rolled = AugmentRoller.roll(config, VaultGearData.read(stack).getItemLevel(), new Random());
        AugmentItemData.setRolled(stack, rolled);
        AugmentItemData.recipes(stack).keySet().forEach(ability -> AugmentItemData.setRecipe(stack, ability, null));
        source.sendSuccess(new TextComponent("Rerolled (recipes cleared). Augments: " + (rolled.isEmpty() ? "none" : formatTokens(rolled))), true);
        return 1;
    }

    private static FocusAugmentsConfig requireConfig(CommandSourceStack source) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            source.sendFailure(new TextComponent("The focus augments table is not loaded; see the server log."));
        }
        return config;
    }

    private static ItemStack heldItem(CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getOffhandItem();
        if (!AugmentRoller.canCarry(stack)) {
            source.sendFailure(new TextComponent("Hold a wand or focus in your off hand."));
            return null;
        }
        return stack;
    }

    private static String formatTokens(Map<AugmentType, Integer> tokens) {
        List<String> parts = new ArrayList<>();
        tokens.forEach((type, count) -> parts.add(type.displayName() + " x" + count + " (" + type.id().toLowerCase(Locale.ROOT) + ")"));
        return String.join(", ", parts);
    }
}

package xyz.iwolfking.woldsvaults.milestones;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MilestoneCommand {
    private MilestoneCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvmilestones")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIds)
                                        .executes(MilestoneCommand::get))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIds)
                                        .then(Commands.argument("value", LongArgumentType.longArg(0))
                                                .executes(MilestoneCommand::set)))))
                .then(Commands.literal("grant-tier")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIds)
                                        .executes(MilestoneCommand::grantTier))))
                .then(Commands.literal("list")
                        .executes(context -> list(context, null))
                        .then(Commands.argument("category", StringArgumentType.word()).suggests(MilestoneCommand::suggestCategories)
                                .executes(context -> list(context, StringArgumentType.getString(context, "category"))))));
    }

    private static CompletableFuture<Suggestions> suggestIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MilestoneRegistry.getAll().forEach(definition -> builder.suggest(definition.getId()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCategories(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (MilestoneCategory category : MilestoneCategory.values()) {
            builder.suggest(category.getId());
        }
        return builder.buildFuture();
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        MilestoneDefinition definition = MilestoneRegistry.get(id);
        if (definition == null) {
            context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
            return 0;
        }
        long value = Milestones.getValue(player, id);
        context.getSource().sendSuccess(new TextComponent(player.getGameProfile().getName() + " " + id + " = " + value
                + " (tier " + definition.getCompletedTiers(value) + "/" + definition.getTierCount()
                + ", next " + nextThreshold(definition, value) + ")"), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        if (!MilestoneRegistry.contains(id)) {
            context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
            return 0;
        }
        long value = LongArgumentType.getLong(context, "value");
        Milestones.setExact(player, id, value);
        context.getSource().sendSuccess(new TextComponent("Set " + id + " to " + value + " for "
                + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int grantTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        MilestoneDefinition definition = MilestoneRegistry.get(id);
        if (definition == null) {
            context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
            return 0;
        }
        long value = Milestones.getValue(player, id);
        int tier = definition.getCompletedTiers(value);
        if (tier >= definition.getTierCount()) {
            context.getSource().sendFailure(new TextComponent(id + " is already fully completed"));
            return 0;
        }
        Milestones.setExact(player, id, definition.getThreshold(tier));
        context.getSource().sendSuccess(new TextComponent("Granted tier " + (tier + 1) + " of " + id + " to "
                + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, String categoryId) {
        List<MilestoneDefinition> definitions;
        if (categoryId == null) {
            definitions = MilestoneRegistry.getAll();
        } else {
            MilestoneCategory category = MilestoneCategory.byId(categoryId);
            if (category == null) {
                context.getSource().sendFailure(new TextComponent("Unknown category: " + categoryId));
                return 0;
            }
            definitions = MilestoneRegistry.getByCategory(category);
        }
        context.getSource().sendSuccess(new TextComponent(definitions.size() + " milestones"), false);
        for (MilestoneDefinition definition : definitions) {
            StringBuilder tiers = new StringBuilder();
            for (int tier = 0; tier < definition.getTierCount(); tier++) {
                if (tier > 0) {
                    tiers.append('/');
                }
                tiers.append(definition.getThreshold(tier)).append('=').append(definition.getReputation(tier)).append("rep");
            }
            context.getSource().sendSuccess(new TextComponent(definition.getCategory().getId() + " " + definition.getId()
                    + " [" + tiers + "]"), false);
        }
        return definitions.size();
    }

    private static String nextThreshold(MilestoneDefinition definition, long value) {
        int tier = definition.getCompletedTiers(value);
        return tier >= definition.getTierCount() ? "done" : String.valueOf(definition.getThreshold(tier));
    }
}

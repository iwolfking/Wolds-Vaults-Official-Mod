package xyz.iwolfking.woldsvaults.milestones;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import iskallia.vault.world.data.PlayerGreedTreeData;
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
                .then(Commands.literal("set-tier")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIds)
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(0))
                                                .executes(MilestoneCommand::setTier)))))
                .then(Commands.literal("claim")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIdsOrAll)
                                        .executes(MilestoneCommand::claim))))
                .then(Commands.literal("pin")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.word()).suggests(MilestoneCommand::suggestIdsOrNone)
                                        .executes(MilestoneCommand::pin))))
                .then(Commands.literal("earned")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MilestoneCommand::earned)))
                .then(Commands.literal("list")
                        .executes(context -> list(context, null))
                        .then(Commands.argument("category", StringArgumentType.word()).suggests(MilestoneCommand::suggestCategories)
                                .executes(context -> list(context, StringArgumentType.getString(context, "category"))))));
    }

    private static CompletableFuture<Suggestions> suggestIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MilestoneRegistry.getAll().forEach(definition -> builder.suggest(definition.getId()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestIdsOrAll(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("all");
        return suggestIds(context, builder);
    }

    private static CompletableFuture<Suggestions> suggestIdsOrNone(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("none");
        return suggestIds(context, builder);
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
                + ", next " + nextThreshold(definition, value)
                + ", claimed " + MilestoneData.get(player.server).getClaimedTiers(player.getUUID(), id)
                + ", unclaimed " + Milestones.getUnclaimedRep(player.server, player.getUUID(), id) + "rep)"), false);
        return 1;
    }

    /**
     * Read-only reputation accounting for one player. Reports the balance they would be holding had
     * they never ranked up, what the ranks they hold cost on the ladder, and how that compares with
     * the balance actually stored. Nothing is written.
     */
    private static int earned(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String name = player.getGameProfile().getName();
        int claimed = Milestones.getClaimedRep(player.server, player.getUUID());
        int unclaimed = Milestones.getUnclaimedRep(player.server, player.getUUID());
        int rank = PlayerGreedTreeData.get(player.server).getGreedTier(player);
        int held = PlayerGreedTreeData.get(player.server).getGreedReputation(player);
        int spent = MilestoneRankLadder.getCumulativeCost(rank);
        int expected = claimed - spent;
        context.getSource().sendSuccess(new TextComponent(name + " greed reputation:"), false);
        context.getSource().sendSuccess(new TextComponent("  earned from milestones (claimed) : " + claimed), false);
        context.getSource().sendSuccess(new TextComponent("  banked, not yet collected        : " + unclaimed), false);
        String band = rank < MilestoneRankLadder.FIRST_RANK ? "unranked"
                : MilestoneRankLadder.getBandName(rank) + " " + MilestoneRankLadder.getTierInBand(rank);
        context.getSource().sendSuccess(new TextComponent("  rank                             : " + rank
                + " (" + band + ")"), false);
        context.getSource().sendSuccess(new TextComponent("  ladder cost of ranks held        : " + spent), false);
        context.getSource().sendSuccess(new TextComponent("  balance that implies             : " + expected), false);
        context.getSource().sendSuccess(new TextComponent("  balance actually stored          : " + held
                + " (" + (held - expected >= 0 ? "+" : "") + (held - expected) + ")"), false);
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

    /** Forces a milestone to the counter value that completes the given tier; tier 0 resets it. */
    private static int setTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        MilestoneDefinition definition = MilestoneRegistry.get(id);
        if (definition == null) {
            context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
            return 0;
        }
        int tier = IntegerArgumentType.getInteger(context, "tier");
        if (tier > definition.getTierCount()) {
            context.getSource().sendFailure(new TextComponent(id + " has only " + definition.getTierCount() + " tiers"));
            return 0;
        }
        long value = tier == 0 ? 0L : definition.getThreshold(tier - 1);
        Milestones.setExact(player, id, value);
        Milestones.clampClaimedTiers(player, id, tier);
        context.getSource().sendSuccess(new TextComponent("Set " + id + " to tier " + tier + " (" + value + ") for "
                + player.getGameProfile().getName()), true);
        return 1;
    }

    /** Claims without the greed trader container gate the real claim message enforces. */
    private static int claim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        int reputation;
        if ("all".equals(id)) {
            reputation = 0;
            for (MilestoneDefinition definition : MilestoneRegistry.getAll()) {
                reputation += Milestones.claimUnchecked(player, definition.getId());
            }
        } else {
            if (!MilestoneRegistry.contains(id)) {
                context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
                return 0;
            }
            reputation = Milestones.claimUnchecked(player, id);
        }
        MilestoneFlusher.syncAll(player);
        context.getSource().sendSuccess(new TextComponent("Claimed " + reputation + " reputation from " + id + " for "
                + player.getGameProfile().getName()), true);
        return reputation;
    }

    private static int pin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String id = StringArgumentType.getString(context, "id");
        String target = "none".equals(id) ? null : id;
        if (!Milestones.pin(player, target)) {
            context.getSource().sendFailure(new TextComponent("Unknown milestone: " + id));
            return 0;
        }
        context.getSource().sendSuccess(new TextComponent(target == null
                ? "Cleared the pinned milestone for " + player.getGameProfile().getName()
                : "Pinned " + id + " for " + player.getGameProfile().getName()), true);
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

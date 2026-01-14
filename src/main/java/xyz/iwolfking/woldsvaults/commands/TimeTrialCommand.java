package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.api.core.competition.PlayerRewardStorage;
import xyz.iwolfking.woldsvaults.api.core.competition.lib.RewardBundle;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.client.screens.TimeTrialLeaderboardEntry;
import xyz.iwolfking.woldsvaults.api.core.competition.TimeTrialCompetition;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.menu.TimeTrialRewardsGui;
import xyz.iwolfking.woldsvaults.network.packets.TimeTrialLeaderboardS2CPacket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TimeTrialCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("timetrial")
                        .requires(source -> source.hasPermission(0))
                        .requires(TimeTrialCommand::isCompetitionEnabled)

                        .then(Commands.literal("leaderboard")
                                .executes(TimeTrialCommand::showLeaderboardScreen)
                        )

                        .then(Commands.literal("leaderboard_text")
                                .executes(TimeTrialCommand::showLeaderboardText)
                        )

                        .then(Commands.literal("status")
                                .executes(TimeTrialCommand::showStatus)
                        )

                        .then(Commands.literal("end")
                                .requires(src -> src.hasPermission(2))
                                .executes(TimeTrialCommand::endCompetition)
                        )

                        .then(Commands.literal("reset")
                                .requires(src -> src.hasPermission(2))
                                .executes(TimeTrialCommand::resetCompetition)
                        )

                        .then(Commands.literal("set_objective")
                                .then(Commands.argument("objective", StringArgumentType.word()).suggests(TimeTrialCommand::suggestObjectiveIds)
                                .requires(src -> src.hasPermission(2))
                                .executes(TimeTrialCommand::setObjective))
                        )

                        .then(Commands.literal("rewards")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    new TimeTrialRewardsGui(player).open();
                                    return 1;
                                })
                        )

                        .then(Commands.literal("quick_claim_rewards")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    PlayerRewardStorage storage =
                                            PlayerRewardStorage.get(player.getServer());

                                    List<RewardBundle> bundles =
                                            storage.getRewards(player.getUUID());

                                    if (bundles.isEmpty()) {
                                        player.sendMessage(
                                                new TextComponent("§7You have no unclaimed rewards."),
                                                player.getUUID()
                                        );
                                        return 0;
                                    }

                                    for (RewardBundle bundle : bundles) {
                                        for (ItemStack stack : bundle.getItems()) {
                                            if (!player.addItem(stack.copy())) {
                                                player.drop(stack.copy(), false);
                                            }
                                        }
                                    }

                                    storage.clearRewards(player.getUUID());

                                    player.sendMessage(
                                            new TextComponent("§aAll rewards claimed!"),
                                            player.getUUID()
                                    );
                                    return 1;
                                }))

                        .executes(context -> {
                            context.getSource().sendFailure(
                                    new TextComponent("Usage: /timetrial <leaderboard|status|end|reset|rewards>")
                            );
                            return 0;
                        })
        );
    }


    private static int showLeaderboardScreen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        if (competition == null) {
            context.getSource().sendFailure(new TextComponent("Time Trial competition is not available right now."));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        TimeTrialCompetition comp = TimeTrialCompetition.get();

        List<TimeTrialLeaderboardEntry> entries =
                comp.getLeaderboard().entrySet().stream()
                        .limit(28)
                        .map(e -> new TimeTrialLeaderboardEntry(
                                e.getKey(),
                                comp.getPlayerName(e.getKey()),
                                e.getValue()
                        ))
                        .toList();

        ModNetwork.sendToClient(
                new TimeTrialLeaderboardS2CPacket(
                        comp.getCurrentObjective(),
                        comp.getTimeRemaining(),
                        entries
                ), player);

        return 1;
    }


    private static int showLeaderboardText(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        if (competition == null) {
            context.getSource().sendFailure(new TextComponent("Time Trial competition is not available right now."));
            return 0;
        }

        Map<UUID, Long> leaderboard = competition.getLeaderboard();
        if (leaderboard.isEmpty()) {
            context.getSource().sendSuccess(
                    new TextComponent("No one has completed the Time Trial yet this week!").withStyle(ChatFormatting.YELLOW),
                    false);
            return 1;
        }

        MutableComponent message = new TextComponent("\n§6§l===== [ §eWeekly Time Trial Leaderboard §6] =====\n")
                .append(new TextComponent(String.format("§7Current Objective: §e%s\n\n", competition.getCurrentObjective())));

        int position = 1;
        for (Map.Entry<UUID, Long> entry : leaderboard.entrySet()) {
            String playerName = competition.getPlayerName(entry.getKey());
            double time = entry.getValue() / 20.0;
            
            ChatFormatting positionColor = position == 1 ? ChatFormatting.GOLD : 
                                         position == 2 ? ChatFormatting.GRAY : 
                                         position == 3 ? ChatFormatting.YELLOW : 
                                         ChatFormatting.WHITE;
            
            message.append(new TextComponent(String.format("%s#%d. §7%s: §e%.2fs\n",
                    positionColor, position++, playerName, time)));
            
            if (position > 10) break; // Show top 10
        }

        long timeLeft = competition.getTimeRemaining();
        long days = TimeUnit.MILLISECONDS.toDays(timeLeft);
        long hours = TimeUnit.MILLISECONDS.toHours(timeLeft) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft));
        
        message.append(new TextComponent(String.format("\n§7Time remaining: §e%d days, %d hours, %d minutes",
                days, hours, minutes)));
        
        context.getSource().sendSuccess(message, false);
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        if (competition == null) {
            context.getSource().sendFailure(new TextComponent("Time Trial competition is not available right now."));
            return 0;
        }

        long timeLeft = competition.getTimeRemaining();
        long days = TimeUnit.MILLISECONDS.toDays(timeLeft);
        long hours = TimeUnit.MILLISECONDS.toHours(timeLeft) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft));
        
        Component message = new TextComponent("\n§6§l===== [ §eWeekly Time Trial §6] =====\n")
                .append(new TextComponent(String.format("§7Current Objective: §e%s\n",
                        competition.getCurrentObjective())))
                .append(new TextComponent(String.format("§7Time Remaining: §e%d days, %d hours, %d minutes\n",
                        days, hours, minutes)))
                .append(new TextComponent(String.format("§7Participants: §e%d\n",
                        competition.getLeaderboard().size())));
        
        context.getSource().sendSuccess(message, false);
        return 1;
    }

    private static int endCompetition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        competition.endCompetition(ServerLifecycleHooks.getCurrentServer());
        return 1;
    }

    private static int resetCompetition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        competition.resetCompetition();
        return 1;
    }

    private static int setObjective(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TimeTrialCompetition competition = TimeTrialCompetition.get();
        competition.setCurrentObjective(StringArgumentType.getString(context, "objective"));
        return 1;
    }

    private static boolean isCompetitionEnabled(CommandSourceStack source) {
        return TimeTrialCompetition.isCompetitionEnabled(source.getServer());
    }

    public static CompletableFuture<Suggestions> suggestObjectiveIds(CommandContext<CommandSourceStack> context,SuggestionsBuilder builder) {
        for(String key : CrystalData.OBJECTIVE.keys()) {
            builder.suggest(key);
        }

        return builder.buildFuture();
    }
}

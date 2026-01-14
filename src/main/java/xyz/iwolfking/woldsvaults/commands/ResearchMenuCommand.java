package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.UsernameCache;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.util.MessageFunctions;
import xyz.iwolfking.woldsvaults.menu.PlayerResearchesGui;
import xyz.iwolfking.woldsvaults.menu.TimeTrialRewardsGui;

import java.util.concurrent.CompletableFuture;

public class ResearchMenuCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("researches").then(Commands.argument("player", StringArgumentType.word()).suggests(ResearchMenuCommand::suggestAnyPlayer).executes(AddCommand -> triggerEvent(AddCommand.getSource(),
                StringArgumentType.getString(AddCommand, "player")
        ))));
    }

    private static int triggerEvent(CommandSourceStack ctx, String target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getPlayerOrException();
        new PlayerResearchesGui(player, target).open();
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestAnyPlayer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for(String name : UsernameCache.getMap().values()) {
            builder.suggest(name);
        }

        return builder.buildFuture();
    }
}

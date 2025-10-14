package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.UsernameCache;
import xyz.iwolfking.woldsvaults.gui.server.PlayerResearchesGUI;
import xyz.iwolfking.woldsvaults.util.MessageFunctions;

import java.util.UUID;

public class PlayerResearchCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("researchs").then(Commands.argument("player", MessageArgument.message()).executes(AddCommand -> openResearchGUI(AddCommand.getSource(),
                MessageArgument.getMessage(AddCommand, "player").getString()
        ))));
    }

    private static int openResearchGUI(CommandSourceStack ctx, String target) throws CommandSyntaxException {
        if (UsernameCache.getMap().containsValue(target)) {
            if (ctx.getServer().getProfileCache().get(target).isPresent()) {
                UUID uuid = ctx.getServer().getProfileCache().get(target).get().getId();
                PlayerResearchesGUI gui = new PlayerResearchesGUI(ctx.getPlayerOrException(), uuid);
                gui.updateDisplay();
                gui.setTitle(new TextComponent(target + "'s Researches"));
                gui.open();
                return 0;

            }
        } else {
            MessageFunctions.sendMessage(ctx.getPlayerOrException(), new TextComponent("Could not find a player with that username."));
            return 0;
        }
        return 0;
    }
}

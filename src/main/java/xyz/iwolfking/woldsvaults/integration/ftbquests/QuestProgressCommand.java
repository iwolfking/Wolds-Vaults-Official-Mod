package xyz.iwolfking.woldsvaults.integration.ftbquests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.Collection;

/**
 * {@code /wvquests complete <targets> <ids>}: completes FTB Quests objects for players. The ids are plain text and are
 * looked up when the command runs, so a data pack function using it loads at boot, before the quest file exists.
 */
public final class QuestProgressCommand {
    private QuestProgressCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvquests")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("complete")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("ids", StringArgumentType.greedyString())
                                        .executes(QuestProgressCommand::complete)))));
    }

    private static int complete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        CommandSourceStack source = context.getSource();
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) {
            WoldLogger.error("FTB Quests has no server quest file; no quest progress was changed");
            source.sendFailure(new TextComponent("The quest file is not loaded."));
            return 0;
        }
        int completed = 0;
        for (String id : StringArgumentType.getString(context, "ids").trim().split("\\s+")) {
            QuestObjectBase object = file.getBase(file.getID(id));
            if (object == null) {
                WoldLogger.error("Quest object {} does not exist; skipped it", id);
                source.sendFailure(new TextComponent("No quest object with id " + id));
                continue;
            }
            for (ServerPlayer player : targets) {
                ProgressChange change = new ProgressChange(file);
                change.origin = object;
                change.reset = false;
                change.player = player.getUUID();
                object.forceProgress(file.getData(player), change);
            }
            completed++;
        }
        source.sendSuccess(new TextComponent("Completed " + completed + " quest objects for " + targets.size() + " players"), true);
        return completed;
    }
}

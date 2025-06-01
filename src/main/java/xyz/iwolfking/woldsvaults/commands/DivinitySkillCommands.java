package xyz.iwolfking.woldsvaults.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityData;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityStatData;

public class DivinitySkillCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("woldsvaults")
                .requires(sender -> sender.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("divinity")
                        .then(Commands.literal("resetDivinityTree")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(DivinitySkillCommands::resetDivinityTree)
                                )
                        )
                        .then(Commands.literal("addDivinityPoints")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(DivinitySkillCommands::addDivinityPoints)
                                        )

                                )
                        )
                )

        );
    }

    private static int resetDivinityTree(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        PlayerDivinityData data = PlayerDivinityData.get(ServerLifecycleHooks.getCurrentServer());
        data.resetDivinityTree(player);
        context.getSource().sendSuccess(new TextComponent("Reset Divinity Skills of Player: " + player.getName().getString()), true);

        return 0;
    }

    private static int addDivinityPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        PlayerDivinityStatData data = PlayerDivinityStatData.get(player.getLevel());
        data.addDivinityPoints(player, amount);
        context.getSource().sendSuccess(new TextComponent("Added " + amount + " of Divinity points to Player: " + player.getName().getString()), true);

        return 0;
    }
}

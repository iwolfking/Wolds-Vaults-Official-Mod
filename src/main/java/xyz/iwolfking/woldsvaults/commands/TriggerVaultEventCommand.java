package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.UsernameCache;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.util.MessageFunctions;
import xyz.iwolfking.woldsvaults.gui.server.PlayerResearchesGUI;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TriggerVaultEventCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vault_event").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("id", ResourceLocationArgument.id()).suggests(TriggerVaultEventCommand::suggestVaultEventIds).executes(AddCommand -> triggerEvent(AddCommand.getSource(),
                EntityArgument.getPlayer(AddCommand, "player"),
                ResourceLocationArgument.getId(AddCommand,"id")
        )))));
    }

    private static int triggerEvent(CommandSourceStack ctx, ServerPlayer target, ResourceLocation id) throws CommandSyntaxException {
        if(!ctx.getPlayerOrException().hasPermissions(3)) {
            MessageFunctions.sendMessage(ctx.getPlayerOrException(), new TextComponent("Only OPs can use this command."));
            return 0;
        }

        VaultUtils.getVault(target.getLevel()).ifPresent(vault -> {
            VaultEventSystem.triggerEvent(id, target::getOnPos, target, vault);
        });

        return 0;
    }

    public static CompletableFuture<Suggestions> suggestVaultEventIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (ResourceLocation id : VaultEventSystem.getAllEventIds()) {
            builder.suggest(id.toString());
        }

        return builder.buildFuture();
    }
}

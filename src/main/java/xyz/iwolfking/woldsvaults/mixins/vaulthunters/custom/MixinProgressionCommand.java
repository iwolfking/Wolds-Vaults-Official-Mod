package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.command.progression.ProgressionCommand;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.Collection;

@Mixin(value = ProgressionCommand.class, remap = false)
public class MixinProgressionCommand {

    /** Adds {@code /the_vault progression research unlock_all <targets>}, which researches every research at once. */
    @Inject(method = "build", at = @At("TAIL"))
    private void woldsvaults$addResearchUnlockAll(LiteralArgumentBuilder<CommandSourceStack> builder, CallbackInfo ci) {
        builder.then(Commands.literal("research")
                .then(Commands.literal("unlock_all")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(MixinProgressionCommand::woldsvaults$unlockAllResearches))));
    }

    @Unique
    private static int woldsvaults$unlockAllResearches(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        CommandSourceStack source = context.getSource();
        if (ModConfigs.RESEARCHES == null) {
            WoldLogger.error("Research config is not loaded; no researches were unlocked");
            source.sendFailure(new TextComponent("The research config is not loaded."));
            return 0;
        }
        int total = 0;
        for (ServerPlayer player : targets) {
            PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
            ResearchTree tree = data.getResearches(player);
            int unlocked = 0;
            for (Research research : ModConfigs.RESEARCHES.getAll()) {
                if (!tree.isResearched(research)) {
                    data.research(player, research, false);
                    unlocked++;
                }
            }
            total += unlocked;
            source.sendSuccess(new TextComponent("Unlocked " + unlocked + " researches for "
                    + player.getGameProfile().getName()), true);
        }
        return total;
    }
}

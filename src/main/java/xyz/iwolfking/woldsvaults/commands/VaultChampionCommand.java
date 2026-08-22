package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionKills;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionManager;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionSpawner;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampionState;

/**
 * Testing and diagnostics for the Vault Champion. Everything it does is reachable in normal play by
 * building rage; this exists so the fight can be tuned without farming a vault first, and so the rage
 * curve can be read directly rather than inferred.
 */
public class VaultChampionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvchampion").requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn").executes(VaultChampionCommand::spawn))
                .then(Commands.literal("rage").executes(VaultChampionCommand::readRage)
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(VaultChampionCommand::addRage))));
    }

    private static int spawn(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Vault vault = VaultUtils.getVault(player.level).orElse(null);
        if (vault == null || !(player.level instanceof ServerLevel level)) {
            return fail(context, "Not inside a vault.");
        }
        GreedMedallionTier tier = GreedMedallionVaultState.get(vault).orElse(null);
        if (tier == null) {
            return fail(context, "This vault carries no greed medallion.");
        }
        if (!tier.vaultChampionHunts()) {
            return fail(context, "The " + tier.getPathName() + " medallion is below the Vault Champion gate.");
        }
        VaultChampionState.PlayerState state = VaultChampionState.get(vault, player.getUUID());
        if (state != null && state.getLiveChampion() != null) {
            return fail(context, "You already have a live Vault Champion.");
        }
        TheVesselEntity champion = VaultChampionSpawner.spawn(vault, tier, level, player);
        if (champion == null) {
            return fail(context, "No valid spawn position was found.");
        }
        if (state != null) {
            state.setLiveChampion(champion.getUUID());
        }
        VaultChampionKills.syncBar(vault, champion);
        context.getSource().sendSuccess(new TextComponent("Spawned a Vault Champion with a pool of "
                + (long) VaultChampionKills.poolOf(champion) + ".").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int readRage(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Vault vault = VaultUtils.getVault(player.level).orElse(null);
        if (vault == null) {
            return fail(context, "Not inside a vault.");
        }
        VaultChampionState.PlayerState state = VaultChampionState.get(vault, player.getUUID());
        if (state == null) {
            return fail(context, "No rage state for this vault.");
        }
        GreedChampionConfig.Rage rage = VaultChampion.config().getRage();
        double threshold = state.threshold(rage.baseThreshold, rage.thresholdMultiplierPerKill);
        boolean bossFight = player.level instanceof ServerLevel level
                && VaultChampionManager.bossFightInProgress(level);
        context.getSource().sendSuccess(new TextComponent(String.format(
                "rage %.0f / %.0f  |  kills %d  |  armed %s  |  live %s  |  boss fight blocking %s",
                state.getRage(), threshold, state.getChampionKills(), state.isArmed(),
                state.getLiveChampion() != null, bossFight)).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int addRage(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Vault vault = VaultUtils.getVault(player.level).orElse(null);
        if (vault == null) {
            return fail(context, "Not inside a vault.");
        }
        VaultChampionState.PlayerState state = VaultChampionState.get(vault, player.getUUID());
        if (state == null) {
            return fail(context, "No rage state for this vault.");
        }
        state.addRage(DoubleArgumentType.getDouble(context, "amount"));
        return readRage(context);
    }

    private static int fail(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(new TextComponent(message).withStyle(ChatFormatting.RED));
        return 0;
    }
}

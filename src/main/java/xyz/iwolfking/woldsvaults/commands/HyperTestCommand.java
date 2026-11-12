package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperCycleManager;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperEscalationManager;

import java.util.Optional;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Operator test tools for the HYPER objective, run from inside a hyper vault: skip to a later
 * boss cycle with the modifiers the skipped brutal and boss kills would have added, arm the
 * boss podium, or start the boss fight outright.
 */
public class HyperTestCommand {
    private static final int MAX_CYCLE = 100;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvhyper")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("cycle")
                        .then(Commands.argument("cycle", IntegerArgumentType.integer(1, MAX_CYCLE))
                                .executes(context -> skipToCycle(context.getSource(), IntegerArgumentType.getInteger(context, "cycle")))))
                .then(Commands.literal("arm").executes(context -> arm(context.getSource())))
                .then(Commands.literal("fight")
                        .executes(context -> fight(context.getSource(), -1))
                        .then(Commands.argument("cycle", IntegerArgumentType.integer(1, MAX_CYCLE))
                                .executes(context -> fight(context.getSource(), IntegerArgumentType.getInteger(context, "cycle"))))));
    }

    private record Target(ServerPlayer player, Vault vault, HyperVaultObjective objective) {
    }

    private static Optional<Target> resolve(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<Vault> vault = VaultUtils.getVault(player.getLevel());
        if (vault.isEmpty()) {
            source.sendFailure(new TextComponent("You are not inside a vault."));
            return Optional.empty();
        }
        Optional<HyperVaultObjective> objective = HyperVaultObjective.get(vault.get());
        if (objective.isEmpty()) {
            source.sendFailure(new TextComponent("This vault has no HYPER objective."));
            return Optional.empty();
        }
        if (objective.get().getCycleManager() == null) {
            source.sendFailure(new TextComponent("The HYPER objective has not started yet; try again in a moment."));
            return Optional.empty();
        }
        return Optional.of(new Target(player, vault.get(), objective.get()));
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        Optional<Target> target = resolve(source);
        if (target.isEmpty()) {
            return 0;
        }
        HyperVaultObjective objective = target.get().objective();
        BlockPos pillar = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        source.sendSuccess(new TextComponent("HYPER cycle " + objective.getOr(HyperVaultObjective.CYCLE, 0)
                + ", phase " + objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING)
                + ", chaos " + objective.getOr(HyperVaultObjective.CHAOS_COUNT, 0) + "/" + HyperVaultObjective.cfg().getChaosCap()
                + ", podium " + (pillar == null ? "not found yet" : pillar.toShortString())).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    /**
     * Replays every kill between the current cycle and {@code cycle} in game order: the
     * batch's brutal pillar bosses, then the hyperboss. The current batch only pays for the
     * brutal bosses it has left; an armed podium means they are all dead already.
     */
    private static int skipToCycle(CommandSourceStack source, int cycle) throws CommandSyntaxException {
        Optional<Target> resolved = resolve(source);
        if (resolved.isEmpty()) {
            return 0;
        }
        Target target = resolved.get();
        HyperVaultObjective objective = target.objective();
        Phase phase = objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING);
        int current = objective.getOr(HyperVaultObjective.CYCLE, 0);
        if (phase == Phase.FIGHT) {
            source.sendFailure(new TextComponent("A boss fight is in progress; finish it before skipping cycles."));
            return 0;
        }
        if (cycle <= current) {
            source.sendFailure(new TextComponent("The vault is already at cycle " + current + "; cycles can only be skipped forward."));
            return 0;
        }

        HyperCycleManager cycles = objective.getCycleManager();
        HyperEscalationManager escalation = objective.getEscalationManager();
        Object2IntMap<VaultModifier<?>> before = target.vault().get(Vault.MODIFIERS).getDisplayGroup();
        JavaRandom random = JavaRandom.ofNanoTime();
        int brutalKills = 0;
        for (int c = current; c < cycle; c++) {
            int bosses;
            if (c > current || phase == Phase.ROLLING || phase == Phase.REWARD) {
                bosses = cycles.rollBrutalBossCount(c, random);
            } else if (phase == Phase.MINIS) {
                bosses = cycles.remainingBrutalBosses();
            } else {
                bosses = 0;
            }
            cycles.simulateBrutalKills(bosses);
            brutalKills += bosses;
            escalation.simulateBossKill();
        }

        if (phase == Phase.REWARD) {
            escalation.clearExitPillar();
        }
        if (phase != Phase.ARMED) {
            objective.set(HyperVaultObjective.PHASE, Phase.ROLLING);
        }

        Object2IntMap<VaultModifier<?>> after = target.vault().get(Vault.MODIFIERS).getDisplayGroup();
        int added = 0;
        for (Object2IntMap.Entry<VaultModifier<?>> entry : after.object2IntEntrySet()) {
            int delta = entry.getIntValue() - before.getOrDefault(entry.getKey(), 0);
            if (delta > 0) {
                source.sendSuccess(new TextComponent(" + ").append(entry.getKey().getChatDisplayNameComponent(delta)), false);
                added += delta;
            }
        }
        String summary = "Skipped HYPER cycle " + current + " -> " + cycle + ": " + brutalKills + " brutal kills and "
                + (cycle - current) + " boss kills replayed, " + added + " modifier stacks added (chaos "
                + objective.getOr(HyperVaultObjective.CHAOS_COUNT, 0) + "/" + HyperVaultObjective.cfg().getChaosCap() + ").";
        source.sendSuccess(new TextComponent(summary).withStyle(ChatFormatting.GOLD), true);
        HyperVaultObjective.broadcast(target.vault(), "[Test] The Vault jumps to HYPER cycle " + cycle + ".", ChatFormatting.LIGHT_PURPLE);
        WoldLogger.info("{} used /wvhyper cycle: {}", target.player().getScoreboardName(), summary);
        return 1;
    }

    private static int arm(CommandSourceStack source) throws CommandSyntaxException {
        Optional<Target> target = resolve(source);
        if (target.isEmpty()) {
            return 0;
        }
        return armPodium(source, target.get()) ? 1 : 0;
    }

    private static boolean armPodium(CommandSourceStack source, Target target) {
        HyperVaultObjective objective = target.objective();
        Phase phase = objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING);
        if (phase == Phase.FIGHT) {
            source.sendFailure(new TextComponent("A boss fight is already in progress."));
            return false;
        }
        if (phase == Phase.REWARD) {
            objective.getEscalationManager().clearExitPillar();
        }
        objective.set(HyperVaultObjective.PHASE, Phase.ARMED);
        HyperVaultObjective.broadcast(target.vault(), "[Test] The boss podium is armed at HYPER cycle "
                + objective.getOr(HyperVaultObjective.CYCLE, 0) + ".", ChatFormatting.RED);
        return true;
    }

    private static int fight(CommandSourceStack source, int cycle) throws CommandSyntaxException {
        Optional<Target> resolved = resolve(source);
        if (resolved.isEmpty()) {
            return 0;
        }
        Target target = resolved.get();
        HyperVaultObjective objective = target.objective();
        if (cycle > 0 && cycle != objective.getOr(HyperVaultObjective.CYCLE, 0)
                && skipToCycle(source, cycle) == 0) {
            return 0;
        }
        if (!armPodium(source, target)) {
            return 0;
        }
        BlockPos pillar = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (pillar == null || !target.player().getLevel().isLoaded(pillar)) {
            source.sendFailure(new TextComponent("The podium is armed, but its position is not known or not loaded yet. "
                    + "Walk to the boss room and click it with an empty hand."));
            return 0;
        }
        objective.getBossManager().armAndStartFight(pillar);
        if (objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING) != Phase.FIGHT) {
            source.sendFailure(new TextComponent("The fight did not start (see the server log). The podium stays armed; click it with an empty hand."));
            return 0;
        }
        source.sendSuccess(new TextComponent("Started the HYPER cycle " + objective.getOr(HyperVaultObjective.CYCLE, 0)
                + " boss fight at " + pillar.toShortString() + ".").withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}

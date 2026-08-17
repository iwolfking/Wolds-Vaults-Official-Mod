package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.vault.EntityState;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultFactory;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.layout.RebirthCrystalLayout;
import iskallia.vault.item.crystal.modifiers.DefaultCrystalModifiers;
import iskallia.vault.item.crystal.objective.RebirthCrystalObjective;
import iskallia.vault.item.crystal.time.ValueCrystalTime;
import iskallia.vault.util.EntityHelper;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.UUID;

/**
 * Server-side entry and exit for greed rank-up trials: validate the requirements, build the trial
 * vault in memory and drop the player straight into it, then pay out (or not) when it ends.
 *
 * <p>The build-and-join path is the base mod's own crystal-free one, copied from
 * {@code ServerboundRebirthMessage}: fill a {@link CrystalData} that never becomes an item,
 * {@link VaultFactory#create} it, {@link ServerVaults#add} it, then add the initiator as the only
 * {@link Runner}. No crystal is consumed and none is produced.</p>
 *
 * <p>Trials are personal. Both trial flavors pin {@code ClassicListenersLogic.MAX_PLAYERS} to 1, so
 * a party member cannot tag along at all - which is the simplest correct reading of "only the
 * initiating player ranks up", since nobody else can be in the vault to begin with.</p>
 */
public final class GreedTrials {
    /** Ticks the vault stays open after a trial is won, matching every other vault's outro. */
    public static final int VICTORY_TICKS = 300;
    /** Shorter outro on a failed trial - there is nothing to celebrate and nothing to collect. */
    public static final int FAILURE_TICKS = 100;

    private GreedTrials() {
    }

    /**
     * Handles a "Take Trial" click. Re-checks every requirement server side and refuses with a
     * chat line rather than silently doing nothing, so a stale client button is diagnosable.
     */
    public static void take(ServerPlayer player) {
        if (ServerVaults.isInVault(player.level)) {
            refuse(player, "You cannot start a rank-up trial from inside a Vault.");
            return;
        }
        int rank = GreedTrialRequirements.nextRank(player);
        GreedTrial trial = GreedTrial.forRank(rank);
        if (trial == null) {
            refuse(player, "There is no rank-up trial beyond Legend - further ranks are earned with reputation alone.");
            return;
        }
        if (!GreedTrialRequirements.hasReputation(player, rank)) {
            refuse(player, "You need " + MilestoneRankLadder.getThreshold(rank) + " greed reputation to attempt this trial.");
            return;
        }
        if (!GreedTrialRequirements.hasGodLevel(player, rank)) {
            refuse(player, "You need level " + MilestoneRankLadder.getGodLevelGate(rank) + " with a Vault God to attempt this trial.");
            return;
        }
        switch (trial.getKind()) {
            case HYPER -> startHyper(player, trial);
            case VESSEL -> startVessel(player, trial);
        }
    }

    /**
     * A toned-down hyper vault. Difficulty, modifier budget, boss strength and per-cycle scaling
     * all come off the trial row instead of hyper_objective.json; the crystal objective carries
     * the target rank so the objective can mark itself the moment it is built.
     */
    private static void startHyper(ServerPlayer player, GreedTrial trial) {
        HyperVaultCrystalObjective objective = new HyperVaultCrystalObjective();
        objective.setTrialRank(trial.getTargetRank());
        CrystalData crystal = CrystalData.empty();
        crystal.setObjective(objective);
        crystal.setTime(new ValueCrystalTime(IntRoll.ofConstant(trial.getTicks())));
        DefaultCrystalModifiers modifiers = new DefaultCrystalModifiers();
        modifiers.setRandomModifiers(false);
        crystal.setModifiers(modifiers);
        crystal.getProperties().setLevel(vaultLevel(player));
        open(player, crystal, trial, null);
    }

    /**
     * A single-phase vessel fight on the base mod's rebirth arena. The rebirth crystal objective
     * pins the clock to infinite and the player count to one; the infinite flag is stripped after
     * the fact so the trial's five-minute limit can run.
     *
     * <p>The vault level is deliberately left at zero, which is what the base mod's own greed
     * trial runs at. The sheet specifies the vessel's numbers and nothing else, and the arena's
     * phase-transition add waves are the only other thing level would move.</p>
     */
    private static void startVessel(ServerPlayer player, GreedTrial trial) {
        CrystalData crystal = CrystalData.empty();
        crystal.setLayout(new RebirthCrystalLayout());
        crystal.setObjective(new RebirthCrystalObjective());
        crystal.setTime(new ValueCrystalTime(IntRoll.ofConstant(trial.getTicks())));
        DefaultCrystalModifiers modifiers = new DefaultCrystalModifiers();
        modifiers.setRandomModifiers(false);
        crystal.setModifiers(modifiers);
        open(player, crystal, trial, RebirthCrystalLayout.INITIAL_ENTRY_POS);
    }

    /**
     * Creates the vault, marks it as a trial and puts the initiator in it as the sole runner.
     * {@code entry} is the fixed arena spawn for layouts that have no classic start portal to
     * teleport into; passing null leaves the join to the vault's own keep-in-vault logic.
     */
    private static void open(ServerPlayer player, CrystalData crystal, GreedTrial trial, Vec3 entry) {
        Vault vault;
        try {
            vault = VaultFactory.create(Version.latest(), crystal, player);
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Building the rank {} greed trial vault for {} failed!",
                    trial.getTargetRank(), player.getGameProfile().getName(), e);
            refuse(player, "The trial could not be prepared. Check the server log.");
            return;
        }
        vault.ifPresent(Vault.LISTENERS, listeners -> {
            Object logic = listeners.get(Listeners.LOGIC);
            if (logic instanceof ClassicListenersLogic classic) {
                classic.set(ClassicListenersLogic.MAX_PLAYERS, 1);
            } else {
                WoldsVaults.LOGGER.warn("Greed trial vault has no ClassicListenersLogic — the solo-only gate is not applied.");
            }
        });
        vault.ifPresent(Vault.CLOCK, clock -> {
            clock.remove(TickClock.INFINITE);
            clock.set(TickClock.DISPLAY_TIME, trial.getTicks());
        });
        applyTrialModifiers(vault, trial);
        ServerVaults.add(vault);
        GreedTrialData.get(player.server).mark(vault.get(Vault.ID), trial.getTargetRank());
        VirtualWorld world = ServerVaults.getWorld(vault).orElse(null);
        if (world == null) {
            WoldsVaults.LOGGER.error("Greed trial vault {} has no virtual world — the player was not sent in.", vault.get(Vault.ID));
            refuse(player, "The trial world could not be opened. Check the server log.");
            return;
        }
        vault.ifPresent(Vault.LISTENERS, listeners -> listeners.add(world, vault,
                (Listener) new Runner().set(Runner.JOIN_STATE, new EntityState(player)).set(Runner.ID, player.getUUID())));
        if (entry != null) {
            VaultUtils.changeDimension(world, player, entry, Vec3.ZERO, 0.0F, 0.0F, p -> {
            });
        }
        player.displayClientMessage(new TextComponent("Rank-up trial started.").withStyle(ChatFormatting.GOLD), false);
    }

    /** Difficulty lock for hyper trials; vessel trials inherit the arena's own fixed difficulty. */
    private static void applyTrialModifiers(Vault vault, GreedTrial trial) {
        if (trial.getDifficulty() == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, VaultMod.id(trial.getDifficulty().getModifierName()), 1);
    }

    private static int vaultLevel(ServerPlayer player) {
        return PlayerVaultStatsData.get(player.getLevel()).getVaultStats((Player) player).getVaultLevel();
    }

    /**
     * Pays a won trial: one rank up (the coherence hook on {@code setGreedTier} floors reputation
     * to the new band) and the flat coin purse for the rank climbed. Only the vault owner is paid,
     * which for a trial is the only player who can be inside it.
     */
    public static void award(Vault vault, ServerPlayer player) {
        UUID vaultId = vault.get(Vault.ID);
        int rank = GreedTrialData.get(player.server).getTargetRank(vaultId);
        if (rank <= 0) {
            WoldsVaults.LOGGER.error("Greed trial award requested for vault {} which is not marked as a trial — nothing paid.", vaultId);
            return;
        }
        UUID owner = vault.getOptional(Vault.OWNER).orElse(null);
        if (owner != null && !owner.equals(player.getUUID())) {
            WoldsVaults.LOGGER.info("Skipping greed trial payout for {}: the trial belongs to {}.",
                    player.getGameProfile().getName(), owner);
            return;
        }
        GreedTrialData.get(player.server).clear(vaultId);
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(player.server);
        int current = treeData.getGreedTier(player);
        if (current + 1 != rank) {
            WoldsVaults.LOGGER.warn("Greed trial for rank {} finished while {} sits at rank {}; setting the rank to {} anyway.",
                    rank, player.getGameProfile().getName(), current, rank);
        }
        treeData.setGreedTier(player, rank);
        int coins = MilestoneRankLadder.getTrialCoinReward(rank);
        if (coins > 0) {
            EntityHelper.giveItem((Player) player, new ItemStack(ModItems.GREED_COIN, coins));
        }
        player.displayClientMessage(new TextComponent("Rank-up trial passed! ").withStyle(ChatFormatting.GOLD)
                .append((Component) new TextComponent("+" + coins + " Greed Coins").withStyle(ChatFormatting.YELLOW)), false);
    }

    /** Drops the trial mark without paying anything - death, bail or a timeout all land here. */
    public static void forfeit(Vault vault, ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return;
        }
        GreedTrialData.get(player.getServer()).clear(vault.get(Vault.ID));
        player.displayClientMessage(new TextComponent("Rank-up trial failed.").withStyle(ChatFormatting.RED), false);
    }

    /** The rank a live vault is a trial for, or 0. Safe to call on any vault, on any thread tick. */
    public static int trialRank(Vault vault) {
        if (vault == null || !vault.has(Vault.ID)) {
            return 0;
        }
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return 0;
        }
        return GreedTrialData.get(server).getTargetRank(vault.get(Vault.ID));
    }

    /** The trial row a live vault is running, or null when it is an ordinary vault. */
    public static GreedTrial trial(Vault vault) {
        return GreedTrial.forRank(trialRank(vault));
    }

    /** True when this vault is a hyper rank-up trial, the gate on every hyper behavior change. */
    public static boolean isHyperTrial(Vault vault) {
        GreedTrial trial = trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.HYPER
                && HyperVaultObjective.get(vault).isPresent();
    }

    private static void refuse(ServerPlayer player, String reason) {
        player.displayClientMessage(new TextComponent(reason).withStyle(ChatFormatting.RED), false);
    }
}

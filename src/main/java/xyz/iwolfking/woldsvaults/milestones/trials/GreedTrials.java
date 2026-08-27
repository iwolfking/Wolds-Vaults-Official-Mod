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
 * Server-side entry and exit for greed rank-up trials. The vault is built in memory from a
 * {@link CrystalData} that never becomes an item, and is pinned to one player.
 */
public final class GreedTrials {
    public static final int VICTORY_TICKS = 300;
    public static final int FAILURE_TICKS = 100;

    private GreedTrials() {
    }

    /** Handles a "Take Trial" click; re-checks every requirement and refuses in chat. */
    public static void take(ServerPlayer player) {
        if (ServerVaults.isInVault(player.level)) {
            refuse(player, "You cannot start a rank-up trial from inside a Vault.");
            return;
        }
        if (isAlreadyInAVault(player)) {
            refuse(player, "You already have a Vault open. Finish or leave it before taking a rank-up trial.");
            return;
        }
        if (PlayerGreedTreeData.get(player.server).getGreedTier(player) < MilestoneRankLadder.FIRST_RANK) {
            refuse(player, "Defeat the Herald to join the greed ladder before taking a rank-up trial.");
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

    /** Whether the player is a listener of any live vault, or owner of one marked as a trial. */
    private static boolean isAlreadyInAVault(ServerPlayer player) {
        UUID playerId = player.getUUID();
        for (Vault vault : ServerVaults.getAll()) {
            Listeners listeners = vault.getOptional(Vault.LISTENERS).orElse(null);
            if (listeners != null && listeners.contains(playerId)) {
                return true;
            }
            if (trialRank(vault) > 0 && playerId.equals(vault.getOptional(Vault.OWNER).orElse(null))) {
                return true;
            }
        }
        return false;
    }

    /** Opens a hyper vault whose numbers come off the trial row, not {@code hyper_objective.json}. */
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

    /** Opens a single-phase vessel fight on the base mod's rebirth arena, at vault level 0. */
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

    /** Creates and marks the vault and adds the sole runner; {@code entry} may be null. */
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

    /**
     * Every trial keeps the runner's inventory on death; a row may also open with free Phoenix
     * stacks and, for a hyper row, its difficulty floor.
     */
    private static void applyTrialModifiers(Vault vault, GreedTrial trial) {
        VaultModifierUtils.addModifier(vault, VaultMod.id("map_afterlife"), 1);
        if (trial.getPhoenixStacks() > 0) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("phoenix"), trial.getPhoenixStacks());
        }
        if (trial.getDifficulty() == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, VaultMod.id(trial.getDifficulty().getModifierName()), 1);
    }

    private static int vaultLevel(ServerPlayer player) {
        return PlayerVaultStatsData.get(player.getLevel()).getVaultStats((Player) player).getVaultLevel();
    }

    /** Pays a won trial to the vault owner: the marked rank plus its coin purse. */
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

    /** Drops the trial mark without paying anything. */
    public static void forfeit(Vault vault, ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return;
        }
        GreedTrialData.get(player.getServer()).clear(vault.get(Vault.ID));
        player.displayClientMessage(new TextComponent("Rank-up trial failed.").withStyle(ChatFormatting.RED), false);
    }

    /** The rank a live vault is a trial for, or 0. Safe to call on any vault. */
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

    /** True when this vault is a hyper rank-up trial running a hyper objective. */
    public static boolean isHyperTrial(Vault vault) {
        GreedTrial trial = trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.HYPER
                && HyperVaultObjective.get(vault).isPresent();
    }

    private static void refuse(ServerPlayer player, String reason) {
        player.displayClientMessage(new TextComponent(reason).withStyle(ChatFormatting.RED), false);
    }
}

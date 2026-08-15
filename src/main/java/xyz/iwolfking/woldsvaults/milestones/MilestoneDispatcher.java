package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.ChaosObjective;
import iskallia.vault.core.vault.objective.ElixirObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.RuneBossObjective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.elixir.ElixirGoal;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.task.ChaosTask;
import iskallia.vault.task.Task;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;

import java.util.Optional;
import java.util.UUID;

/**
 * The single owner of every {@code CommonEvents} listener the milestone engine needs. One
 * listener is registered per event, at common setup, and fans out internally to the counters —
 * so the number of registered references on any vault event grows by exactly one no matter how
 * many milestones exist.
 */
public class MilestoneDispatcher {
    private static final Object OWNER = new Object();
    private static final ResourceLocation ORE_GROUP = VaultMod.id("ores");
    private static final ResourceLocation DUNGEON_BOSS_GROUP = VaultMod.id("mob_type/dungeon_boss");
    private static final ResourceLocation COCKROACH = ResourceLocation.fromNamespaceAndPath("alexsmobs", "cockroach");
    private static final int POLL_INTERVAL = 20;
    static final String CHAIN_ATTACKER_TAG = "woldsvaults_chain_attacker";

    private static boolean registered;

    private MilestoneDispatcher() {
    }

    public static ResourceLocation getDungeonBossGroup() {
        return DUNGEON_BOSS_GROUP;
    }

    public static ResourceLocation getCockroachId() {
        return COCKROACH;
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CommonEvents.CHEST_LOOT_GENERATION.post().register(OWNER, data -> onChestLooted(data.getPlayer(), data.getState()));
        CommonEvents.TREASURE_ROOM_OPEN.register(OWNER, data -> onTreasureDoor(data.getPlayer()));
        CommonEvents.VENDOOR_ROOM_OPEN.register(OWNER, data -> onVendoor(data.getPlayer()));
        CommonEvents.DUNGEON_ROOM_OPEN.register(OWNER, data -> onDungeonDoor(data.getPlayer()));
        CommonEvents.ENTITY_CHAIN_ATTACKED.register(OWNER, data -> {
            if (!(data.getAttacker() instanceof ServerPlayer player)) {
                return;
            }
            for (net.minecraft.world.entity.Mob mob : data.getAttackedMobs()) {
                mob.getPersistentData().putUUID(CHAIN_ATTACKER_TAG, player.getUUID());
            }
        });
        CommonEvents.ENTITY_DAMAGE_BLOCK.blockSucceeded().register(OWNER, data -> {
            if (data.getAttacked() instanceof ServerPlayer player) {
                Milestones.advance(player, MilestoneIds.DEFENSE, 1L);
            }
        });
        CommonEvents.MANA_MODIFY.register(OWNER, data -> {
            if (data.getAction() != ManaAction.PLAYER_ACTION) {
                return;
            }
            float spent = data.getOldAmount() - data.getNewAmount();
            if (spent > 0.0F && data.getPlayer() instanceof ServerPlayer player) {
                Milestones.advanceFractional(player, MilestoneIds.SPELL_SPAMMER, spent);
            }
        });
        CommonEvents.DISCOVER_ROOM_EVENT.register(OWNER, data -> {
            if (data.getRoomId() != null && data.getRoomId().getPath().contains("/omega")
                    && data.getPlayer() instanceof ServerPlayer player) {
                Milestones.advance(player, MilestoneIds.LEGENDARY, 1L);
            }
        });
        CommonEvents.CHALLENGE_ROOM_COMPLETE.register(OWNER, data -> {
            for (UUID playerId : data.getPlayers()) {
                ServerPlayer player = data.getVirtualWorld().getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    Milestones.advance(player, MilestoneIds.CHALLENGED, 1L);
                }
            }
        });
        CommonEvents.BLACK_MARKET_TRADE.register(OWNER, data -> Milestones.advance(data.getPlayer(), MilestoneIds.WANTED_CRIMINAL, 1L));
        CommonEvents.GOD_ALTAR_EVENT.register(OWNER, data -> {
            if (!data.isCompleted()) {
                return;
            }
            forEachRunner(data.getVault(), player -> Milestones.advance(player, MilestoneIds.SEND_A_PRAYER, 1L));
        });
        CommonEvents.VAULT_MODIFIER_REMOVE.register(OWNER, data -> {
            if (data.modifier == null || data.modifier.getId() == null) {
                return;
            }
            if (data.modifier.getId().getPath().contains("phoenix")) {
                forEachRunner(data.vault, player -> Milestones.advance(player, MilestoneIds.BORN_AGAIN, 1L));
            }
        });
        CommonEvents.LISTENER_JOIN.register(OWNER, data ->
                MilestoneVaultState.get(data.getVault().get(Vault.ID), data.getListener().get(Listener.ID)));
        CommonEvents.LISTENER_TICK.register(OWNER, data -> onListenerTick(data.getVault(), data.getListener()));
        CommonEvents.LISTENER_LEAVE.register(OWNER, data -> onListenerLeave(data.getVault(), data.getListener()));
        CommonEvents.VAULT_END.register(OWNER, data -> {
            MilestoneVaultState.release(data.getVault().get(Vault.ID));
            MilestoneFlusher.flushNow(data.getVault());
        });

        WoldsVaults.LOGGER.info("Milestone dispatcher registered {} milestones", MilestoneRegistry.getAll().size());
    }

    private static void onChestLooted(ServerPlayer player, BlockState state) {
        if (player == null || state == null) {
            return;
        }
        Block block = state.getBlock();
        if (!(block instanceof VaultChestBlock chest)) {
            return;
        }
        VaultChestType type = chest.getType();
        boolean box = block instanceof VaultBarrelBlock;
        if (box) {
            if (type == VaultChestType.WOODEN) {
                Milestones.advance(player, MilestoneIds.AMAZON_WORKER, 1L);
            }
        } else {
            String milestone = switch (type) {
                case WOODEN -> MilestoneIds.WOODEN_LOOTER;
                case GILDED -> MilestoneIds.GILDED_LOOTER;
                case ORNATE -> MilestoneIds.ORNATE_LOOTER;
                case LIVING -> MilestoneIds.LIVING_LOOTER;
                default -> null;
            };
            if (milestone != null) {
                Milestones.advance(player, milestone, 1L);
            }
        }
        MilestoneVaultState state1 = stateFor(player);
        if (state1 != null) {
            state1.onChestLooted(type, box);
        }
    }

    private static void onTreasureDoor(Player source) {
        if (!(source instanceof ServerPlayer player)) {
            return;
        }
        Milestones.advance(player, MilestoneIds.TREASURE_HUNTER, 1L);
        MilestoneVaultState state = stateFor(player);
        if (state != null) {
            state.onTreasureDoor();
        }
    }

    private static void onVendoor(Player source) {
        if (!(source instanceof ServerPlayer player)) {
            return;
        }
        Milestones.advance(player, MilestoneIds.SHOP_HUNTER, 1L);
        MilestoneVaultState state = stateFor(player);
        if (state != null) {
            state.onVendoor();
        }
    }

    private static void onDungeonDoor(Player source) {
        if (!(source instanceof ServerPlayer player)) {
            return;
        }
        MilestoneVaultState state = stateFor(player);
        if (state != null) {
            state.onDungeonDoor();
        }
    }

    /**
     * Routes a vault block break. Ore-group blocks feed "Diggy Diggy Jewel", nullite ore feeds
     * "Nullified"; both also feed the per-vault composite tracker.
     */
    public static void onBlockMined(ServerPlayer player, BlockState state) {
        if (player == null || state == null) {
            return;
        }
        MilestoneVaultState vaultState = MilestoneVaultState.current(player.getUUID());
        if (vaultState == null) {
            return;
        }
        if (state.getBlock() == xyz.iwolfking.woldsvaults.init.ModBlocks.NULLITE_ORE) {
            Milestones.advance(player, MilestoneIds.NULLIFIED, 1L);
        }
        if (!MilestoneGroups.isOre(state)) {
            return;
        }
        Milestones.advance(player, MilestoneIds.DIGGY_DIGGY_JEWEL, 1L);
        vaultState.onOreMined();
    }

    private static void onListenerTick(Vault vault, Listener listener) {
        ServerPlayer player = listener.getPlayer().orElse(null);
        if (player == null) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        MilestoneVaultState state = MilestoneVaultState.get(vaultId, player.getUUID());
        Objectives objectives = vault.get(Vault.OBJECTIVES);

        Milestones.advance(player, MilestoneIds.I_LIVE_HERE_NOW, 1L);
        if (!objectives.getAll(SurvivalObjective.class).isEmpty()) {
            Milestones.advance(player, MilestoneIds.I_WILL_SURVIVE, 1L);
        }
        if (player.tickCount % POLL_INTERVAL != 0) {
            return;
        }
        pollObjectives(player, objectives, state);
    }

    /**
     * Objective totals that have no player-attributed event are polled once a second and turned
     * into deltas against a per-vault baseline. The baseline starts at whatever the objective
     * already reports, so re-joining a vault never double counts.
     */
    private static void pollObjectives(ServerPlayer player, Objectives objectives, MilestoneVaultState state) {
        boolean first = !state.areBaselinesReady();

        Optional<BingoObjective> bingo = objectives.findFirst(BingoObjective.class);
        int bingoLines = bingo.map(BingoObjective::getBingos).orElse(0);
        Optional<BallisticBingoObjective> ballistic = objectives.findFirst(BallisticBingoObjective.class);
        if (ballistic.isPresent()) {
            bingoLines = Math.max(bingoLines, ballistic.get().getBingos());
        }
        int bingoDelta = state.takeBingoDelta(bingoLines);

        int scavengerLines = objectives.findFirst(ScavengerBingoObjective.class)
                .map(ScavengerBingoObjective::getCompletedBingos).orElse(0);
        int scavengerDelta = state.takeScavengerBingoDelta(scavengerLines);

        int chaos = objectives.findFirst(ChaosObjective.class).map(objective -> {
            Task task = objective.get(ChaosObjective.TASK);
            return task instanceof ChaosTask chaosTask ? chaosTask.getCompletedCount() : 0;
        }).orElse(0);
        int chaosDelta = state.takeChaosDelta(chaos);

        int elixir = objectives.findFirst(ElixirObjective.class).map(objective -> {
            ElixirGoal goal = objective.get(ElixirObjective.GOALS).get(player.getUUID());
            return goal == null ? 0 : goal.get(ElixirGoal.CURRENT);
        }).orElse(0);
        int elixirDelta = state.takeElixirDelta(elixir);

        int runeBosses = objectives.findFirst(RuneBossObjective.class).map(objective -> {
            RuneBossFights fights = objective.get(RuneBossObjective.FIGHTS);
            return fights == null ? 0 : fights.getCompletedFightsSize();
        }).orElse(0);
        int runeDelta = state.takeRuneBossDelta(runeBosses);

        if (first) {
            state.markBaselinesReady();
            return;
        }
        Milestones.advance(player, MilestoneIds.BINGO, bingoDelta);
        Milestones.advance(player, MilestoneIds.SCAVINGO, scavengerDelta);
        Milestones.advance(player, MilestoneIds.CHAOS_OBJECTIVES, chaosDelta);
        Milestones.advance(player, MilestoneIds.DRINK_UP, elixirDelta);
        Milestones.advance(player, MilestoneIds.RUNIC_RITUAL, runeDelta);
    }

    private static void onListenerLeave(Vault vault, Listener listener) {
        ServerPlayer player = listener.getPlayer().orElse(null);
        UUID vaultId = vault.get(Vault.ID);
        UUID playerId = listener.get(Listener.ID);
        if (player == null) {
            MilestoneVaultState.release(vaultId, playerId);
            return;
        }
        StatCollector stats = vault.getOptional(Vault.STATS).map(collector -> collector.get(listener)).orElse(null);
        Completion completion = stats == null ? null : stats.getOptional(StatCollector.COMPLETION).orElse(null);
        String objective = vault.getOptional(Vault.OBJECTIVES)
                .flatMap(objectives -> objectives.getOptional(Objectives.KEY)).orElse(null);
        MilestoneVaultState state = MilestoneVaultState.peek(vaultId, playerId);

        if (completion == Completion.COMPLETED) {
            Milestones.advance(player, MilestoneIds.VAULTS_HUNTED, 1L);
            if (objective != null) {
                String seenToken = MilestoneRegistry.seenItAllToken(objective);
                if (seenToken != null) {
                    Milestones.addToken(player, MilestoneIds.SEEN_IT_ALL, seenToken);
                }
                if ("royale".equals(objective) || "vault_royale".equals(objective) || "royale_pvp".equals(objective)) {
                    Milestones.advance(player, MilestoneIds.ROYALE_PAINE, 1L);
                }
                if ("ascension".equals(objective)) {
                    Milestones.advance(player, MilestoneIds.STAIRWAY_TO_HEAVEN, 1L);
                }
            }
            if (VaultMapTierCache.get(vaultId) >= 0) {
                Milestones.advance(player, MilestoneIds.EXPLORER, 1L);
            }
            if (state != null) {
                if (state.isFlawless()) {
                    Milestones.advance(player, MilestoneIds.FLAWLESS_VICTORY, 1L);
                }
                if (state.isVaultOfVaultsDone()) {
                    Milestones.complete(player, MilestoneIds.VAULT_OF_VAULTS);
                }
            }
        } else if (completion == Completion.FAILED) {
            Milestones.advance(player, MilestoneIds.FAIL_VAULTS, 1L);
        }
        if (state != null && state.isDedicatedLooterDone()) {
            Milestones.complete(player, MilestoneIds.DEDICATED_LOOTER);
        }
        MilestoneVaultState.release(vaultId, playerId);
        MilestoneData.get(player.server).flush();
    }

    public static void forEachRunner(Vault vault, java.util.function.Consumer<ServerPlayer> action) {
        if (vault == null) {
            return;
        }
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(action);
        }
    }

    static MilestoneVaultState stateFor(ServerPlayer player) {
        return MilestoneVaultState.current(player.getUUID());
    }

    static Vault vaultOf(Level level) {
        return ServerVaults.get(level).orElse(null);
    }

    static boolean isDungeonBoss(net.minecraft.world.entity.Entity entity) {
        return ModConfigs.ENTITY_GROUPS.isInGroup(DUNGEON_BOSS_GROUP, entity);
    }

    static ResourceLocation getOreGroup() {
        return ORE_GROUP;
    }
}

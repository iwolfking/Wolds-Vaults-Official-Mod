package xyz.iwolfking.woldsvaults.armory.service;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.world.data.LevelSyncData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import iskallia.vault.world.data.PlayerReputationData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.SavedBuild;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Creative Armory's raises: the lowest vault level and god levels at which the saved build fits, god
 * reputation up to the charting minimum, and the saved prestige powers the player lacks. Everything only goes up.
 */
public final class CreativeRaise {
    private static final int MAX_GOD_LEVEL = 50;
    private static final int FORCE = 100_000_000;

    private final int level;
    private final Map<VaultGod, Integer> godLevels;
    private final Map<VaultGod, Integer> reputation;
    private final Map<String, Integer> missingPrestige;
    private final TreePlanner.Budget budget;
    private final List<String> notes = new ArrayList<>();

    private CreativeRaise(int level, Map<VaultGod, Integer> godLevels, Map<VaultGod, Integer> reputation,
                          Map<String, Integer> missingPrestige, TreePlanner.Budget budget) {
        this.level = level;
        this.godLevels = godLevels;
        this.reputation = reputation;
        this.missingPrestige = missingPrestige;
        this.budget = budget;
    }

    public TreePlanner.Budget budget() {
        return this.budget;
    }

    public List<String> notes() {
        return this.notes;
    }

    /** Works out the raises for {@code build} against {@code player}'s current state; changes nothing. */
    public static CreativeRaise of(ServerPlayer player, SavedBuild build) {
        TreePlanner.Budget current = TreePlanner.Budget.of(player);
        int greedTier = PlayerGreedTreeData.get(player.getServer()).getGreedTier(player);
        int cap = PlayerVaultStats.getLevelCap(greedTier);
        int level = current.level();
        if (!fits(player, build, withLevel(current, current.level()))) {
            int low = current.level() + 1;
            int high = Math.max(cap, current.level());
            if (low <= high && fits(player, build, withLevel(current, high))) {
                while (low < high) {
                    int mid = (low + high) >>> 1;
                    if (fits(player, build, withLevel(current, mid))) {
                        high = mid;
                    } else {
                        low = mid + 1;
                    }
                }
                level = low;
            }
        }
        TreePlanner.Budget raised = withLevel(current, level);
        Map<VaultGod, Integer> godLevels = new EnumMap<>(raised.godLevels());
        Map<VaultGod, Integer> reputation = new EnumMap<>(raised.reputation());
        GodAlignmentData data = GodAlignmentData.get(player.getServer());
        for (VaultGod god : VaultGod.values()) {
            List<String> saved = build.godNodes(god);
            GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
            if (saved.isEmpty() || tree == null) {
                continue;
            }
            int rep = Math.max(reputation.getOrDefault(god, 0), GodLevels.chartingReputation());
            reputation.put(god, rep);
            int cost = 0;
            for (String id : saved) {
                GodNode node = tree.getNode(id);
                cost += node == null ? 0 : node.cost();
            }
            int lastTransfer = -1;
            List<String> transfers = build.godTransfers(god);
            for (int i = 0; i < transfers.size(); i++) {
                if (!transfers.get(i).isEmpty()) {
                    lastTransfer = i;
                }
            }
            GodAlignmentData.GodState state = data.copyState(player.getUUID(), god);
            int godLevel = godLevels.getOrDefault(god, 0);
            while (godLevel < MAX_GOD_LEVEL && (state.totalPoints(godLevel, GodLevels.reputationPoints(rep)) < cost
                    || GodLevels.minorTransferSlots(godLevel) <= lastTransfer)) {
                godLevel++;
            }
            godLevels.put(god, godLevel);
        }
        Map<String, Integer> missing = new LinkedHashMap<>();
        PrestigeTree prestige = PlayerPrestigePowersData.get(player.getLevel()).getPowers(player);
        for (Map.Entry<String, Integer> entry : build.learned("prestige").entrySet()) {
            Skill skill = prestige.getForId(entry.getKey()).orElse(null);
            int have = skill instanceof TieredSkill tiered ? tiered.getUnmodifiedTier() : skill != null && skill.isUnlocked() ? 1 : 0;
            if (skill != null && have < entry.getValue()) {
                missing.put(entry.getKey(), entry.getValue());
            }
        }
        TreePlanner.Budget budget = new TreePlanner.Budget(raised.level(), raised.skillTotal(), raised.expertiseTotal(),
                godLevels, reputation);
        CreativeRaise raise = new CreativeRaise(level, godLevels, reputation, missing, budget);
        if (level > current.level()) {
            raise.notes.add("Raises vault level " + current.level() + " -> " + level);
        }
        for (VaultGod god : VaultGod.values()) {
            int before = current.godLevels().getOrDefault(god, 0);
            if (godLevels.getOrDefault(god, 0) > before) {
                raise.notes.add("Raises " + god.getName() + " level " + before + " -> " + godLevels.get(god));
            }
            int repBefore = current.reputation().getOrDefault(god, 0);
            if (reputation.getOrDefault(god, 0) > repBefore) {
                raise.notes.add("Raises " + god.getName() + " reputation " + repBefore + " -> " + reputation.get(god));
            }
        }
        if (!missing.isEmpty()) {
            raise.notes.add("Learns " + missing.size() + " missing prestige power" + (missing.size() == 1 ? "" : "s"));
        }
        return raise;
    }

    private static boolean fits(ServerPlayer player, SavedBuild build, TreePlanner.Budget budget) {
        return TreePlanner.plan(player, build, true, false, budget).ok();
    }

    private static TreePlanner.Budget withLevel(TreePlanner.Budget current, int level) {
        int skill = 0;
        int expertise = 0;
        for (int l = current.level() + 1; l <= Math.min(level, 100); l++) {
            skill++;
            if (l % 5 == 0) {
                expertise++;
            }
        }
        return new TreePlanner.Budget(Math.max(level, current.level()), current.skillTotal() + skill,
                current.expertiseTotal() + expertise, current.godLevels(), current.reputation());
    }

    /** Applies the raises to the player (only upward), mirroring the level-up point grants. */
    public void apply(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        PlayerVaultStatsData statsData = PlayerVaultStatsData.get(player.getLevel());
        PlayerVaultStats stats = statsData.getVaultStats(player);
        int before = stats.getVaultLevel();
        if (this.level > before) {
            LevelSyncData.get(server).leaveTeam(player);
            for (int l = before + 1; l <= Math.min(this.level, 100); l++) {
                stats.addSkillPoints(1);
                if (l % 5 == 0) {
                    stats.addExpertisePoints(1);
                }
            }
            stats.setVaultLevel(server, this.level);
            stats.sync(server);
            statsData.setDirty();
            WoldsVaults.LOGGER.info("Creative Armory: raised {} to vault level {} (was {})", player.getGameProfile().getName(),
                    this.level, before);
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        for (VaultGod god : VaultGod.values()) {
            int rep = PlayerReputationData.getReputation(player.getUUID(), god);
            int wantRep = this.reputation.getOrDefault(god, rep);
            if (wantRep > rep) {
                PlayerReputationData.addReputation(player.getUUID(), god, wantRep - rep);
                WoldsVaults.LOGGER.info("Creative Armory: raised {}'s {} reputation {} -> {}", player.getGameProfile().getName(),
                        god.getName(), rep, wantRep);
            }
            GodAlignmentData.GodState state = data.copyState(player.getUUID(), god);
            int current = GodLevels.gatedLevel(state.xp, state.sacrifices);
            int want = this.godLevels.getOrDefault(god, current);
            if (want > current) {
                long oldXp = state.xp;
                data.setLevel(player, god, want);
                if (oldXp > GodLevels.xpForLevel(want)) {
                    GodAlignmentData.GodState after = data.copyState(player.getUUID(), god);
                    after.xp = oldXp;
                    data.putState(player, god, after);
                }
                WoldsVaults.LOGGER.info("Creative Armory: raised {}'s {} level {} -> {}", player.getGameProfile().getName(),
                        god.getName(), current, want);
            }
        }
        if (!this.missingPrestige.isEmpty()) {
            PlayerPrestigePowersData prestigeData = PlayerPrestigePowersData.get(player.getLevel());
            PrestigeTree prestige = prestigeData.getPowers(player);
            SkillContext force = new SkillContext(100_000, FORCE, FORCE, SkillSource.of(player));
            for (Map.Entry<String, Integer> entry : this.missingPrestige.entrySet()) {
                prestige.getForId(entry.getKey()).ifPresent(skill -> {
                    if (skill instanceof TieredSkill tiered) {
                        int target = Math.min(entry.getValue(), tiered.getTiers().size());
                        while (tiered.getUnmodifiedTier() < target) {
                            tiered.learn(force, true);
                        }
                    } else if (skill instanceof LearnableSkill learnable && !skill.isUnlocked()) {
                        learnable.learn(force);
                    }
                });
            }
            prestige.sync(SkillContext.ofPrestige(player));
            prestigeData.setDirty();
            WoldsVaults.LOGGER.info("Creative Armory: taught {} {} prestige powers", player.getGameProfile().getName(),
                    this.missingPrestige.size());
        }
    }
}

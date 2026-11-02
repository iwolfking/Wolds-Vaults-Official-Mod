package xyz.iwolfking.woldsvaults.armory.service;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.GroupedSkill;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.skill.tree.SkillTree;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerReputationData;
import iskallia.vault.world.data.PlayerTalentsData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.SavedBuild;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosAbilityCaps;
import xyz.iwolfking.woldsvaults.gods.ultimates.UltimateSpecializationManager;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PlayerExpertisesDataAccessor;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dry run and commit of a saved page's trees. Each pool is rebuilt on a fresh config copy and learned through the
 * same gates the game's Level messages run, against the player's own point totals: abilities and talents share
 * the skill pool, expertises use expertise points, and each god uses its own derived points. Nothing touches the
 * player until {@link Plan#commit}; the totals are preserved because unspent is rewritten as total minus spent.
 */
public final class TreePlanner {
    private TreePlanner() {
    }

    /** The player's numbers a plan is built against; the Creative Armory raises them before planning. */
    public record Budget(int level, int skillTotal, int expertiseTotal, Map<VaultGod, Integer> godLevels,
                         Map<VaultGod, Integer> reputation) {
        public static Budget of(ServerPlayer player) {
            ServerLevel level = player.getLevel();
            PlayerVaultStats stats = PlayerVaultStatsData.get(level).getVaultStats(player);
            int skill = stats.getUnspentSkillPoints()
                    + PlayerAbilitiesData.get(level).getAbilities(player).getSpentLearnPoints()
                    + PlayerTalentsData.get(level).getTalents(player).getSpentLearnPoints();
            int expertise = stats.getUnspentExpertisePoints()
                    + PlayerExpertisesData.get(level).getExpertises(player).getSpentLearnPoints();
            Map<VaultGod, Integer> godLevels = new EnumMap<>(VaultGod.class);
            Map<VaultGod, Integer> reputation = new EnumMap<>(VaultGod.class);
            GodAlignmentData data = GodAlignmentData.get(player.getServer());
            for (VaultGod god : VaultGod.values()) {
                GodAlignmentData.GodState state = data.copyState(player.getUUID(), god);
                godLevels.put(god, GodLevels.gatedLevel(state.xp, state.sacrifices));
                reputation.put(god, PlayerReputationData.getReputation(player.getUUID(), god));
            }
            return new Budget(stats.getVaultLevel(), skill, expertise, godLevels, reputation);
        }
    }

    /** A checked plan. Refusals empty means {@link #commit} can run. */
    public static final class Plan {
        public final List<String> refusals = new ArrayList<>();
        public final List<String> notes = new ArrayList<>();
        @Nullable AbilityTree abilities;
        @Nullable String selectedAbility;
        @Nullable TalentTree talents;
        @Nullable ExpertiseTree expertises;
        int skillTotal;
        int expertiseTotal;
        @Nullable Map<VaultGod, GodAlignmentData.GodState> gods;

        public boolean ok() {
            return this.refusals.isEmpty();
        }

        /** Applies the checked trees; the caller has already confirmed {@link #ok()}. */
        public void commit(ServerPlayer player) {
            MinecraftServer server = player.getServer();
            ServerLevel level = player.getLevel();
            if (this.gods != null) {
                GodAlignmentData godData = GodAlignmentData.get(server);
                for (Map.Entry<VaultGod, GodAlignmentData.GodState> entry : this.gods.entrySet()) {
                    godData.putState(player, entry.getKey(), entry.getValue());
                }
                GodNodeCache.refresh(player);
                GodNodeTicker.reconcile(player);
            }
            if (this.abilities != null && this.talents != null) {
                PlayerAbilitiesData.get(level).setAbilities(player, this.abilities);
                SkillContext context = SkillContext.of(player);
                if (this.selectedAbility != null) {
                    this.abilities.onQuickSelect(this.selectedAbility, context);
                }
                this.abilities.sync(context);
                PlayerTalentsData.get(level).setTalents(player, this.talents);
            }
            if (this.expertises != null) {
                PlayerExpertisesData data = PlayerExpertisesData.get(level);
                SkillContext context = SkillContext.of(player);
                data.getExpertises(player).getAll(LearnableSkill.class, Skill::isUnlocked).forEach(skill -> skill.onRemove(context));
                ((PlayerExpertisesDataAccessor) data).woldsvaults$getPlayerMap().put(player.getUUID(), this.expertises);
                this.expertises.getAll(LearnableSkill.class, Skill::isUnlocked).forEach(skill -> skill.onAdd(context));
                data.setDirty();
                this.expertises.sync(context);
            }
            if (this.abilities != null || this.expertises != null) {
                PlayerVaultStatsData statsData = PlayerVaultStatsData.get(level);
                PlayerVaultStats stats = statsData.getVaultStats(player);
                if (this.abilities != null && this.talents != null) {
                    int spent = this.abilities.getSpentLearnPoints() + this.talents.getSpentLearnPoints();
                    stats.setSkillPoints(this.skillTotal - spent);
                    stats.setTotalSpentSkillPoints(spent);
                }
                if (this.expertises != null) {
                    int spent = this.expertises.getSpentLearnPoints();
                    stats.setExpertisePoints(this.expertiseTotal - spent);
                    stats.setTotalSpentExpertisePoints(spent);
                }
                stats.sync(server);
                statsData.setDirty();
            }
            UltimateSpecializationManager.refresh(player);
            if (this.abilities != null) {
                this.abilities.iterate(TieredSkill.class, tiered -> {
                    if (TenosAbilityCaps.isGated(tiered.getId())) {
                        int cap = TenosAbilityCaps.cap(tiered.getId(), player);
                        if (tiered.getUnmodifiedTier() > cap) {
                            WoldsVaults.LOGGER.warn("Armory: {} loaded {} at tier {}, above the Tenos cap {}",
                                    player.getGameProfile().getName(), tiered.getId(), tiered.getUnmodifiedTier(), cap);
                        }
                    }
                });
            }
        }
    }

    /** Checks the pools the upgrades allow against {@code budget}; nothing on the player changes. */
    public static Plan plan(ServerPlayer player, SavedBuild build, boolean skills, boolean gods, Budget budget) {
        Plan plan = new Plan();
        if (skills) {
            planSkills(player, build, budget, plan, !gods);
            planExpertises(build, budget, plan);
        }
        if (gods) {
            planGods(player, build, budget, plan);
        }
        return plan;
    }

    private static void planSkills(ServerPlayer player, SavedBuild build, Budget budget, Plan plan, boolean checkTenosNow) {
        AbilityTree abilities = ModConfigs.ABILITIES.get().map(config -> (AbilityTree) config.copy()).orElse(null);
        TalentTree talents = ModConfigs.TALENTS.get().map(config -> (TalentTree) config.copy()).orElse(null);
        if (abilities == null || talents == null) {
            plan.refusals.add("The ability or talent config is not loaded");
            WoldsVaults.LOGGER.error("Armory: ability or talent config missing; Load refused");
            return;
        }
        int needed = needed(build);
        if (needed > budget.skillTotal()) {
            plan.refusals.add("Skills need " + needed + " points; you have " + budget.skillTotal());
            return;
        }
        SkillContext context = new SkillContext(budget.level(), budget.skillTotal(), SkillSource.empty());
        for (Map.Entry<String, Integer> entry : build.learned("abilities").entrySet()) {
            Skill skill = abilities.getForId(entry.getKey()).orElse(null);
            if (!(skill instanceof LearnableSkill learnable) || !(skill.getParent() instanceof SpecializedSkill specialized)) {
                plan.refusals.add("Ability " + entry.getKey() + " no longer exists");
                continue;
            }
            specialized.specialize(entry.getKey(), context);
            while (tierOf(skill) < entry.getValue()) {
                if (!learnable.canLearn(context)) {
                    plan.refusals.add(skill instanceof TieredSkill tiered ? learnFailure(tiered, nameOf(skill), budget.level())
                            : "Ability " + nameOf(skill) + " cannot be learned");
                    break;
                }
                learnable.learn(context);
            }
            if (checkTenosNow && TenosAbilityCaps.isGated(skill.getId())) {
                int cap = TenosAbilityCaps.cap(skill.getId(), player);
                if (entry.getValue() > cap) {
                    plan.refusals.add(nameOf(skill) + " tier " + entry.getValue() + " is above your Tenos cap of " + cap);
                }
            }
        }
        learnRounds(talents, build.learned("talents"), context, budget.level(), "Talent", plan);
        plan.abilities = abilities;
        plan.talents = talents;
        plan.skillTotal = budget.skillTotal();
        String selected = build.selectedAbility();
        if (selected != null && abilities.getForId(selected).filter(Skill::isUnlocked).isPresent()) {
            plan.selectedAbility = selected;
        }
    }

    private static int needed(SavedBuild build) {
        AbilityTree abilities = build.displayAbilities();
        TalentTree talents = build.displayTalents();
        return (abilities == null ? 0 : abilities.getSpentLearnPoints()) + (talents == null ? 0 : talents.getSpentLearnPoints());
    }

    private static void planExpertises(SavedBuild build, Budget budget, Plan plan) {
        ExpertiseTree expertises = ModConfigs.EXPERTISES.getAll().copy();
        int needed = build.displayExpertises().getSpentLearnPoints();
        if (needed > budget.expertiseTotal()) {
            plan.refusals.add("Expertises need " + needed + " points; you have " + budget.expertiseTotal());
            return;
        }
        SkillContext context = new SkillContext(budget.level(), budget.expertiseTotal(), SkillSource.empty());
        learnRounds(expertises, build.learned("expertises"), context, budget.level(), "Expertise", plan);
        plan.expertises = expertises;
        plan.expertiseTotal = budget.expertiseTotal();
    }

    /**
     * Learns saved tiers in rounds until a round changes nothing: point-spent gates depend on learn order, so a
     * talent locked in one round may open in a later one.
     */
    private static void learnRounds(SkillTree tree, Map<String, Integer> targets, SkillContext context, int level,
                                    String what, Plan plan) {
        Map<String, Integer> pending = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : targets.entrySet()) {
            Skill skill = tree.getForId(entry.getKey()).orElse(null);
            if (skill == null || skill.getParent() instanceof TieredSkill) {
                plan.refusals.add(what + " " + entry.getKey() + " no longer exists");
                continue;
            }
            pending.put(entry.getKey(), entry.getValue());
        }
        boolean progress = true;
        while (progress && !pending.isEmpty()) {
            progress = false;
            for (Map.Entry<String, Integer> entry : new ArrayList<>(pending.entrySet())) {
                String id = entry.getKey();
                Skill skill = tree.getForId(id).orElseThrow();
                if (ModConfigs.SKILL_GATES.getGates().isLocked(id, tree)) {
                    continue;
                }
                LearnableSkill learnable = (LearnableSkill) skill;
                if (skill.getParent() instanceof GroupedSkill grouped) {
                    grouped.select(id);
                    learnable = grouped;
                }
                while (tierOf(skill) < entry.getValue() && learnable.canLearn(context)) {
                    learnable.learn(context);
                    progress = true;
                }
                if (tierOf(skill) >= entry.getValue()) {
                    pending.remove(id);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : pending.entrySet()) {
            Skill skill = tree.getForId(entry.getKey()).orElseThrow();
            String name = nameOf(skill);
            if (ModConfigs.SKILL_GATES.getGates().isLocked(entry.getKey(), tree)) {
                plan.refusals.add(what + " " + name + ": its requirements are not met by the rest of the build");
            } else if (skill instanceof TieredSkill tiered) {
                plan.refusals.add(learnFailure(tiered, name, level));
            } else {
                plan.refusals.add(what + " " + name + " cannot be learned");
            }
        }
    }

    private static String nameOf(Skill skill) {
        return skill.getName() == null ? skill.getId() : skill.getName();
    }

    private static int tierOf(Skill skill) {
        if (skill instanceof TieredSkill tiered) {
            return tiered.getUnmodifiedTier();
        }
        return skill.isUnlocked() ? 1 : 0;
    }

    private static String learnFailure(TieredSkill tiered, String name, int level) {
        int tier = tiered.getUnmodifiedTier();
        if (tier >= tiered.getMaxLearnableTier()) {
            return name + " tier " + (tier + 1) + " is above its learnable cap";
        }
        LearnableSkill next = tiered.getTiers().get(tier);
        if (next.getUnlockLevel() > level) {
            return name + " " + roman(tier + 1) + " needs level " + next.getUnlockLevel() + " (you: " + level + ")";
        }
        return name + " " + roman(tier + 1) + ": not enough points left";
    }

    private static String roman(int value) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return value >= 1 && value <= numerals.length ? numerals[value - 1] : String.valueOf(value);
    }

    private static void planGods(ServerPlayer player, SavedBuild build, Budget budget, Plan plan) {
        GodAlignmentData data = GodAlignmentData.get(player.getServer());
        Map<VaultGod, GodAlignmentData.GodState> out = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            List<String> saved = build.godNodes(god);
            GodAlignmentData.GodState state = data.copyState(player.getUUID(), god);
            if (saved.isEmpty() && state.treeNodes.isEmpty()) {
                continue;
            }
            GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
            if (tree == null) {
                plan.refusals.add("The " + god.getName() + " tree is not loaded");
                continue;
            }
            int reputation = budget.reputation().getOrDefault(god, 0);
            if (!saved.isEmpty() && reputation < GodLevels.chartingReputation()) {
                plan.refusals.add(god.getName() + " needs " + GodLevels.chartingReputation() + " reputation to chart (you: "
                        + reputation + ")");
                continue;
            }
            int level = budget.godLevels().getOrDefault(god, 0);
            int reputationPoints = GodLevels.reputationPoints(reputation);
            state.treeNodes.clear();
            state.spentPoints.clear();
            state.minorTransfers.clear();
            int cost = 0;
            boolean missing = false;
            for (String id : saved) {
                GodNode node = tree.getNode(id);
                if (node == null) {
                    plan.refusals.add(god.getName() + " star " + id + " no longer exists");
                    missing = true;
                } else {
                    cost += node.cost();
                }
            }
            if (missing) {
                continue;
            }
            int total = state.totalPoints(level, reputationPoints);
            if (cost > total) {
                plan.refusals.add(god.getName() + " stars need " + cost + " points; you have " + total);
                continue;
            }
            for (String id : connectedOrder(tree, saved)) {
                GodNode node = tree.getNode(id);
                if (!tree.isPurchasable(id, state.treeNodes::contains)) {
                    plan.refusals.add(god.getName() + " star " + node.id() + " is not reachable");
                    break;
                }
                state.treeNodes.add(id);
                state.spentPoints.merge(node.ledgerKey(), node.cost(), Integer::sum);
            }
            int slots = GodLevels.minorTransferSlots(level);
            List<String> transfers = build.godTransfers(god);
            for (int i = 0; i < transfers.size(); i++) {
                String effect = transfers.get(i);
                if (effect.isEmpty()) {
                    state.minorTransfers.add("");
                    continue;
                }
                if (i >= slots || !MinorTransferSlots.isTransferable(god, effect, state.spentPoints)) {
                    plan.notes.add(god.getName() + " transfer slot " + (i + 1) + " left empty");
                    state.minorTransfers.add("");
                    continue;
                }
                state.minorTransfers.add(effect);
            }
            out.put(god, state);
        }
        plan.gods = out;
    }

    /** Saved nodes ordered so each touches an earlier one or is a root; unconnected ones go last and fail the check. */
    private static List<String> connectedOrder(GodTreeModel tree, List<String> nodeIds) {
        Set<String> wanted = new LinkedHashSet<>(nodeIds);
        Set<String> placed = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        for (String id : wanted) {
            if (tree.getNode(id).isRoot()) {
                queue.add(id);
                placed.add(id);
            }
        }
        while (!queue.isEmpty()) {
            String id = queue.poll();
            for (String neighbour : tree.getAdjacent(id)) {
                if (wanted.contains(neighbour) && placed.add(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }
        List<String> order = new ArrayList<>(placed);
        for (String id : wanted) {
            if (!placed.contains(id)) {
                order.add(id);
            }
        }
        return order;
    }
}

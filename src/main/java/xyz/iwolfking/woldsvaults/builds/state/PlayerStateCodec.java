package xyz.iwolfking.woldsvaults.builds.state;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.GroupedSkill;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.prestige.core.ActivatePrestigePower;
import iskallia.vault.skill.prestige.core.PrestigePower;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.skill.tree.SkillTree;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.LevelSyncData;
import iskallia.vault.world.data.PlayerAbilitiesData;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import iskallia.vault.world.data.PlayerReputationData;
import iskallia.vault.world.data.PlayerTalentsData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.BuildJson;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosAbilityCaps;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PlayerExpertisesDataAccessor;
import xyz.iwolfking.woldsvaults.prestige.CraftingRecipePower;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The non-item half of a build: vault level and points, greed tier, god reputation, the ability, talent, expertise
 * and prestige trees, and the god trees. {@link #capture} writes a player's state; {@link #plan} builds the target
 * state off the player (trees are learned on fresh copies, so nothing touches the player until {@link Plan#apply}).
 */
public final class PlayerStateCodec {
    private static final int FORCE_LEVEL = 100_000;
    private static final int FORCE_POINTS = 100_000_000;
    private static final Set<String> PLAYER_KEYS = Set.of("level", "exp", "points", "spent", "greed", "reputation");
    private static final Set<String> POINT_KEYS = Set.of("skill", "expertise", "knowledge", "prestige", "archetype", "regret");
    private static final Set<String> GOD_KEYS = Set.of("xp", "level", "bonus_points", "sacrifices", "altar_completions",
            "nodes", "transfers");

    private PlayerStateCodec() {
    }

    /** Writes every state section of {@code player} into {@code out}. */
    public static void capture(ServerPlayer player, JsonObject out) {
        MinecraftServer server = player.getServer();
        ServerLevel level = player.getLevel();
        PlayerVaultStats stats = PlayerVaultStatsData.get(level).getVaultStats(player);
        JsonObject playerJson = new JsonObject();
        playerJson.addProperty("level", stats.getVaultLevel());
        playerJson.addProperty("exp", stats.getExp());
        JsonObject points = new JsonObject();
        points.addProperty("skill", stats.getUnspentSkillPoints());
        points.addProperty("expertise", stats.getUnspentExpertisePoints());
        points.addProperty("knowledge", stats.getUnspentKnowledgePoints());
        points.addProperty("prestige", stats.getUnspentPrestigePoints());
        points.addProperty("archetype", stats.getUnspentArchetypePoints());
        points.addProperty("regret", stats.getUnspentRegretPoints());
        playerJson.add("points", points);
        JsonObject spent = new JsonObject();
        spent.addProperty("skill", stats.getTotalSpentSkillPoints());
        spent.addProperty("expertise", stats.getTotalSpentExpertisePoints());
        spent.addProperty("knowledge", stats.getTotalSpentKnowledgePoints());
        spent.addProperty("prestige", stats.getTotalSpentPrestigePoints());
        spent.addProperty("archetype", stats.getTotalSpentArchetypePoints());
        spent.addProperty("regret", stats.getTotalSpentRegretPoints());
        playerJson.add("spent", spent);
        PlayerGreedTreeData greed = PlayerGreedTreeData.get(server);
        JsonObject greedJson = new JsonObject();
        greedJson.addProperty("tier", greed.getGreedTier(player));
        greedJson.addProperty("reputation", greed.getGreedReputation(player));
        playerJson.add("greed", greedJson);
        JsonObject reputation = new JsonObject();
        for (VaultGod god : VaultGod.values()) {
            reputation.addProperty(godKey(god), PlayerReputationData.getReputation(player.getUUID(), god));
        }
        playerJson.add("reputation", reputation);
        out.add("player", playerJson);

        AbilityTree abilityTree = PlayerAbilitiesData.get(level).getAbilities(player);
        JsonObject abilities = new JsonObject();
        JsonObject learned = new JsonObject();
        for (Skill skill : abilityTree.skills) {
            if (skill instanceof SpecializedSkill specialized) {
                LearnableSkill spec = specialized.getSpecialization();
                if (spec instanceof TieredSkill tiered && tiered.getUnmodifiedTier() > 0) {
                    learned.addProperty(tiered.getId(), tiered.getUnmodifiedTier());
                } else if (!(spec instanceof TieredSkill) && spec.isUnlocked()) {
                    learned.addProperty(spec.getId(), 1);
                }
            }
        }
        abilities.add("learned", learned);
        SpecializedSkill selected = abilityTree.getSelected();
        if (selected != null && selected.getId() != null) {
            abilities.addProperty("selected", selected.getId());
        }
        out.add("abilities", abilities);

        out.add("talents", learnedTiers(PlayerTalentsData.get(level).getTalents(player)));
        out.add("expertises", learnedTiers(PlayerExpertisesData.get(level).getExpertises(player)));

        PrestigeTree prestigeTree = PlayerPrestigePowersData.get(level).getPowers(player);
        JsonObject prestige = new JsonObject();
        prestige.add("learned", learnedTiers(prestigeTree));
        JsonArray active = new JsonArray();
        prestigeTree.iterate(TieredSkill.class, tiered -> {
            if (tiered.getChild() instanceof ActivatePrestigePower power && power.isActive() && tiered.getId() != null) {
                active.add(tiered.getId());
            }
        });
        prestige.add("active", active);
        out.add("prestige", prestige);

        GodAlignmentData godData = GodAlignmentData.get(server);
        JsonObject gods = new JsonObject();
        for (VaultGod god : VaultGod.values()) {
            GodAlignmentData.GodState state = godData.copyState(player.getUUID(), god);
            JsonObject godJson = new JsonObject();
            godJson.addProperty("xp", state.xp);
            godJson.addProperty("bonus_points", state.bonusPoints);
            godJson.addProperty("sacrifices", state.sacrifices);
            godJson.addProperty("altar_completions", state.altarCompletions);
            JsonArray nodes = new JsonArray();
            state.treeNodes.forEach(nodes::add);
            godJson.add("nodes", nodes);
            JsonArray transfers = new JsonArray();
            state.minorTransfers.forEach(transfers::add);
            godJson.add("transfers", transfers);
            gods.add(godKey(god), godJson);
        }
        out.add("gods", gods);
    }

    private static JsonObject learnedTiers(SkillTree tree) {
        JsonObject learned = new JsonObject();
        tree.iterate(LearnableSkill.class, skill -> {
            if (skill.getId() == null || skill instanceof GroupedSkill || skill instanceof SpecializedSkill) {
                return;
            }
            if (skill.getParent() instanceof TieredSkill) {
                return;
            }
            if (skill instanceof TieredSkill tiered) {
                if (tiered.getUnmodifiedTier() > 0) {
                    learned.addProperty(tiered.getId(), tiered.getUnmodifiedTier());
                }
            } else if (skill.isUnlocked()) {
                learned.addProperty(skill.getId(), 1);
            }
        });
        return learned;
    }

    public static String godKey(VaultGod god) {
        return god.name().toLowerCase();
    }

    /**
     * Builds the target state a build describes, off the player. Sections the build leaves out stay as they are.
     * Errors throw; game rules the target bypasses go into the report.
     */
    public static Plan plan(JsonObject build, ServerPlayer player, BuildReport report) {
        Plan plan = new Plan();
        ServerLevel level = player.getLevel();
        PlayerVaultStats current = PlayerVaultStatsData.get(level).getVaultStats(player);
        JsonObject playerJson = BuildJson.optObject(build, "player", "build");
        plan.level = current.getVaultLevel();
        int greedTier = PlayerGreedTreeData.get(player.getServer()).getGreedTier(player);
        if (playerJson != null) {
            BuildJson.onlyKeys(playerJson, "player", PLAYER_KEYS);
            Integer levelValue = BuildJson.optInt(playerJson, "level", "player");
            if (levelValue != null) {
                if (levelValue < 0 || levelValue > ModConfigs.LEVELS_META.getMaxLevel()) {
                    throw new BuildException("player.level: must be 0 to " + ModConfigs.LEVELS_META.getMaxLevel());
                }
                plan.level = levelValue;
            }
            Integer exp = BuildJson.optInt(playerJson, "exp", "player");
            plan.exp = exp == null ? 0 : exp;
            JsonObject greed = BuildJson.optObject(playerJson, "greed", "player");
            if (greed != null) {
                BuildJson.onlyKeys(greed, "player.greed", Set.of("tier", "reputation"));
                plan.greedTier = BuildJson.optInt(greed, "tier", "player.greed");
                Integer greedReputation = BuildJson.optInt(greed, "reputation", "player.greed");
                plan.greedReputation = greedReputation == null ? 0 : greedReputation;
                if (plan.greedTier != null) {
                    if (plan.greedTier < 0) {
                        throw new BuildException("player.greed.tier: must be 0 or more");
                    }
                    greedTier = plan.greedTier;
                    int cap = PlayerVaultStats.getLevelCap(greedTier);
                    if (plan.level > cap) {
                        report.bypassed("level " + plan.level + " is above the level cap " + cap + " of greed tier " + greedTier);
                    }
                }
            }
            JsonObject reputation = BuildJson.optObject(playerJson, "reputation", "player");
            if (reputation != null) {
                plan.reputation = new EnumMap<>(VaultGod.class);
                for (Map.Entry<String, JsonElement> entry : reputation.entrySet()) {
                    VaultGod god = god(entry.getKey(), "player.reputation");
                    int value = BuildJson.asInt(entry.getValue(), "player.reputation." + entry.getKey());
                    if (value < 0 || value > 50) {
                        throw new BuildException("player.reputation." + entry.getKey() + ": god reputation runs 0 to 50");
                    }
                    plan.reputation.put(god, value);
                }
            }
        }

        SkillContext force = new SkillContext(FORCE_LEVEL, FORCE_POINTS, FORCE_POINTS, SkillSource.empty());
        JsonObject abilities = BuildJson.optObject(build, "abilities", "build");
        if (abilities != null) {
            BuildJson.onlyKeys(abilities, "abilities", Set.of("learned", "selected"));
            plan.abilities = ModConfigs.ABILITIES.get().orElseThrow(
                    () -> new BuildException("abilities: the ability config is not loaded")).copy();
            JsonObject learned = BuildJson.optObject(abilities, "learned", "abilities");
            if (learned != null) {
                learnAbilities(plan.abilities, learned, plan.level, force, report);
            }
            plan.selectedAbility = BuildJson.optString(abilities, "selected", "abilities");
            if (plan.selectedAbility != null) {
                Skill selected = plan.abilities.getForId(plan.selectedAbility).orElse(null);
                if (!(selected instanceof SpecializedSkill)) {
                    throw new BuildException("abilities.selected: '" + plan.selectedAbility + "' is not an ability");
                }
                if (!selected.isUnlocked()) {
                    report.bypassed("abilities.selected: " + plan.selectedAbility + " is not learned, so it cannot be selected");
                    plan.selectedAbility = null;
                }
            }
        }
        JsonObject talents = BuildJson.optObject(build, "talents", "build");
        if (talents != null) {
            plan.talents = ModConfigs.TALENTS.get().orElseThrow(
                    () -> new BuildException("talents: the talent config is not loaded")).copy();
            learnTree(plan.talents, talents, "talents", plan.level, greedTier, force, report);
        }
        JsonObject expertises = BuildJson.optObject(build, "expertises", "build");
        if (expertises != null) {
            plan.expertises = ModConfigs.EXPERTISES.getAll().copy();
            learnTree(plan.expertises, expertises, "expertises", plan.level, greedTier, force, report);
        }
        JsonObject prestige = BuildJson.optObject(build, "prestige", "build");
        if (prestige != null) {
            BuildJson.onlyKeys(prestige, "prestige", Set.of("learned", "active"));
            plan.prestige = ModConfigs.PRESTIGE_POWERS.get().orElseThrow(
                    () -> new BuildException("prestige: the prestige config is not loaded")).copy();
            JsonObject learned = BuildJson.optObject(prestige, "learned", "prestige");
            if (learned != null) {
                learnTree(plan.prestige, learned, "prestige.learned", plan.level, greedTier, force, report);
            }
            plan.activePrestige = new ArrayList<>();
            JsonArray active = BuildJson.optArray(prestige, "active", "prestige");
            if (active != null) {
                for (int i = 0; i < active.size(); i++) {
                    String id = BuildJson.asString(active.get(i), "prestige.active[" + i + "]");
                    Skill skill = plan.prestige.getForId(id).orElse(null);
                    if (!(skill instanceof TieredSkill tiered) || !(tiered.getChild() instanceof ActivatePrestigePower)) {
                        throw new BuildException("prestige.active[" + i + "]: '" + id + "' is not a learned toggleable prestige power");
                    }
                    plan.activePrestige.add(id);
                }
            }
        }

        if (playerJson != null) {
            plan.stats = statsTag(current, playerJson, plan, report);
        }

        JsonObject gods = BuildJson.optObject(build, "gods", "build");
        if (gods != null) {
            plan.gods = new EnumMap<>(VaultGod.class);
            for (Map.Entry<String, JsonElement> entry : gods.entrySet()) {
                VaultGod god = god(entry.getKey(), "gods");
                String path = "gods." + entry.getKey();
                int reputation = plan.reputation != null && plan.reputation.containsKey(god)
                        ? plan.reputation.get(god) : PlayerReputationData.getReputation(player.getUUID(), god);
                plan.gods.put(god, godState(god, BuildJson.asObject(entry.getValue(), path), path, reputation, report));
            }
        }
        return plan;
    }

    private static VaultGod god(String key, String path) {
        VaultGod god = VaultGod.fromName(key);
        if (god == null) {
            throw new BuildException(path + "." + key + ": no vault god '" + key + "'");
        }
        return god;
    }

    private static void learnAbilities(AbilityTree tree, JsonObject learned, int level, SkillContext force,
                                       BuildReport report) {
        Set<SpecializedSkill> used = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : learned.entrySet()) {
            String id = entry.getKey();
            String path = "abilities.learned." + id;
            int tier = BuildJson.asInt(entry.getValue(), path);
            Skill skill = tree.getForId(id).orElse(null);
            if (skill == null) {
                throw new BuildException(path + ": no ability or specialization '" + id + "' exists in this game");
            }
            if (!(skill instanceof TieredSkill tiered) || !(tiered.getParent() instanceof SpecializedSkill specialized)) {
                throw new BuildException(path + ": '" + id + "' is not an ability specialization (use ids like Javelin_Base)");
            }
            if (!used.add(specialized)) {
                throw new BuildException(path + ": a second specialization of " + specialized.getId()
                        + "; an ability holds one specialization");
            }
            try {
                specialized.specialize(id, force);
                learnTiers(tiered, tier, path, level, -1, force, report);
            } catch (BuildException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new BuildException(path + ": learning it failed (" + e.getClass().getSimpleName() + ": "
                        + e.getMessage() + ")", e);
            }
        }
    }

    private static void learnTree(SkillTree tree, JsonObject learned, String section, int level, int greedTier,
                                  SkillContext force, BuildReport report) {
        for (Map.Entry<String, JsonElement> entry : learned.entrySet()) {
            String id = entry.getKey();
            String path = section + "." + id;
            int tier = BuildJson.asInt(entry.getValue(), path);
            Skill skill = tree.getForId(id).orElse(null);
            if (skill == null) {
                throw new BuildException(path + ": no skill '" + id + "' exists in this game's " + section.split("\\.")[0]);
            }
            if (skill.getParent() instanceof TieredSkill parent) {
                throw new BuildException(path + ": '" + id + "' is one tier of " + parent.getId() + "; use " + parent.getId());
            }
            try {
                if (skill.getParent() instanceof GroupedSkill grouped) {
                    grouped.select(id);
                }
                if (skill instanceof TieredSkill tiered) {
                    learnTiers(tiered, tier, path, level, greedTier, force, report);
                } else if (skill instanceof LearnableSkill learnable && !(skill instanceof GroupedSkill)
                        && !(skill instanceof SpecializedSkill)) {
                    if (tier != 1) {
                        throw new BuildException(path + ": '" + id + "' has a single level; use 1");
                    }
                    if (learnable.getUnlockLevel() > level) {
                        report.bypassed(path + ": needs vault level " + learnable.getUnlockLevel());
                    }
                    learnable.learn(force);
                } else {
                    throw new BuildException(path + ": '" + id + "' is a group, not a learnable skill");
                }
            } catch (BuildException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new BuildException(path + ": learning it failed (" + e.getClass().getSimpleName() + ": "
                        + e.getMessage() + ")", e);
            }
        }
        tree.iterate(GroupedSkill.class, grouped -> {
            if (grouped.getMaxSpentLearnPoints() > 0 && grouped.getSpentLearnPoints() > grouped.getMaxSpentLearnPoints()) {
                report.bypassed(section + ": group " + grouped.getId() + " holds " + grouped.getSpentLearnPoints()
                        + " points, above its limit of " + grouped.getMaxSpentLearnPoints());
            }
        });
        for (String id : learned.keySet()) {
            boolean locked;
            try {
                locked = tree instanceof PrestigeTree prestigeTree
                        ? ModConfigs.SKILL_GATES.getGates().isLocked(id, prestigeTree)
                        : ModConfigs.SKILL_GATES.getGates().isLocked(id, tree);
            } catch (RuntimeException e) {
                report.fallback(section + "." + id + ": could not evaluate its skill gate ("
                        + e.getClass().getSimpleName() + "); not checked");
                continue;
            }
            if (locked) {
                report.bypassed(section + "." + id + ": its skill gate is not met by the rest of the build");
            }
        }
    }

    private static void learnTiers(TieredSkill tiered, int tier, String path, int level, int greedTier,
                                   SkillContext force, BuildReport report) {
        int available = tiered.getTiers().size();
        if (tier < 0 || tier > available) {
            throw new BuildException(path + ": tier " + tier + " is out of range; " + tiered.getId() + " has " + available + " tiers");
        }
        int highestUnlock = 0;
        int highestGreed = 0;
        for (int i = 0; i < tier; i++) {
            LearnableSkill next = tiered.getTiers().get(tiered.getUnmodifiedTier());
            highestUnlock = Math.max(highestUnlock, next.getUnlockLevel());
            if (next instanceof PrestigePower power) {
                highestGreed = Math.max(highestGreed, power.getRequiredGreedTier());
            }
            tiered.learn(force, true);
        }
        if (tier > tiered.getMaxLearnableTier()) {
            report.bypassed(path + ": tier " + tier + " is above the learnable cap of " + tiered.getMaxLearnableTier());
        }
        if (highestUnlock > level) {
            report.bypassed(path + ": tier " + tier + " needs vault level " + highestUnlock + " (build level " + level + ")");
        }
        if (greedTier >= 0 && highestGreed > greedTier) {
            report.bypassed(path + ": needs greed tier " + highestGreed + " (build greed tier " + greedTier + ")");
        }
    }

    private static CompoundTag statsTag(PlayerVaultStats current, JsonObject playerJson, Plan plan, BuildReport report) {
        CompoundTag tag = current.serializeNBT();
        tag.putInt("vaultLevel", plan.level);
        tag.putInt("exp", plan.exp);
        JsonObject points = BuildJson.optObject(playerJson, "points", "player");
        if (points != null) {
            BuildJson.onlyKeys(points, "player.points", POINT_KEYS);
        }
        JsonObject spent = BuildJson.optObject(playerJson, "spent", "player");
        if (spent != null) {
            BuildJson.onlyKeys(spent, "player.spent", POINT_KEYS);
        }
        int skillSpent = (plan.abilities == null ? 0 : plan.abilities.getSpentLearnPoints())
                + (plan.talents == null ? 0 : plan.talents.getSpentLearnPoints());
        int expertiseSpent = plan.expertises == null ? 0 : plan.expertises.getSpentLearnPoints();
        int prestigeSpent = plan.prestige == null ? 0 : plan.prestige.getSpentLearnPoints();
        int knowledgeSpent = 0;
        if (plan.prestige != null) {
            for (PrestigePower power : plan.prestige.getAll(PrestigePower.class, Skill::isUnlocked)) {
                knowledgeSpent += power.getSpentAdditionalLearnPoints();
            }
        }
        spentTotal(tag, spent, "skill", "totalSpentSkillPoints", skillSpent);
        spentTotal(tag, spent, "expertise", "totalSpentExpertisePoints", expertiseSpent);
        spentTotal(tag, spent, "knowledge", "totalSpentKnowledgePoints", knowledgeSpent);
        spentTotal(tag, spent, "prestige", "totalSpentPrestigePoints", prestigeSpent);
        spentTotal(tag, spent, "archetype", "totalSpentArchetypePoints", tag.getInt("totalSpentArchetypePoints"));
        spentTotal(tag, spent, "regret", "totalSpentRegretPoints", tag.getInt("totalSpentRegretPoints"));
        int earnedLevel = Math.min(plan.level, PlayerVaultStats.BASE_LEVEL_CAP);
        unspent(tag, points, "skill", "unspentSkillPts",
                earned(earnedLevel, tag.getInt("totalSpentSkillPoints"), "skill", report), report);
        unspent(tag, points, "expertise", "unspentExpertisePoints",
                earned(earnedLevel / PlayerVaultStats.VAULT_LEVELS_PER_EXPERTISE_POINT,
                        tag.getInt("totalSpentExpertisePoints"), "expertise", report), report);
        unspent(tag, points, "knowledge", "unspentKnowledgePts", 0, report);
        unspent(tag, points, "prestige", "unspentPrestigePoints", 0, report);
        unspent(tag, points, "archetype", "unspentArchetypePoints", 0, report);
        unspent(tag, points, "regret", "unspentRegretPoints", 0, report);
        report.summary("Skill points spent on abilities and talents: " + skillSpent + " (vault level " + plan.level + ")");
        report.summary("Expertise points spent: " + expertiseSpent + "; prestige points spent: " + prestigeSpent
                + "; knowledge spent: " + knowledgeSpent);
        return tag;
    }

    /**
     * The unspent points a player of the build's level would hold: what leveling grants (capped at level 100, as in
     * {@code PlayerVaultStats#addVaultExp}) minus what the build spends. A build that overspends is reported.
     */
    private static int earned(int granted, int spent, String key, BuildReport report) {
        if (spent > granted) {
            report.bypassed("player.points." + key + ": the build spends " + spent + " " + key + " points but leveling grants "
                    + granted);
            return 0;
        }
        return granted - spent;
    }

    private static void unspent(CompoundTag tag, @Nullable JsonObject points, String key, String nbtKey, int auto,
                                BuildReport report) {
        JsonElement value = points == null ? null : points.get(key);
        if (value == null || (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                && "auto".equals(value.getAsString()))) {
            tag.putInt(nbtKey, auto);
            if (auto > 0) {
                report.summary("Unspent " + key + " points: " + auto + " left over from leveling");
            } else if (value == null) {
                report.note("player.points." + key + ": not given; unspent " + key + " points set to 0");
            }
            return;
        }
        int amount = BuildJson.asInt(value, "player.points." + key);
        if (amount < 0) {
            throw new BuildException("player.points." + key + ": must be 0 or more");
        }
        tag.putInt(nbtKey, amount);
    }

    private static void spentTotal(CompoundTag tag, @Nullable JsonObject spent, String key, String nbtKey, int computed) {
        Integer value = spent == null ? null : BuildJson.optInt(spent, key, "player.spent");
        tag.putInt(nbtKey, value == null ? computed : value);
    }

    private static GodAlignmentData.GodState godState(VaultGod god, JsonObject json, String path, int reputation,
                                                      BuildReport report) {
        BuildJson.onlyKeys(json, path, GOD_KEYS);
        GodTreeModel tree = GodNodeRegistry.tree(god).orElseThrow(
                () -> new BuildException(path + ": the " + god.getName() + " god tree is not loaded"));
        GodAlignmentData.GodState state = new GodAlignmentData.GodState();
        Long xp = BuildJson.optLong(json, "xp", path);
        Integer levelValue = BuildJson.optInt(json, "level", path);
        if (xp != null && levelValue != null) {
            throw new BuildException(path + ": give either 'xp' or 'level', not both");
        }
        if (levelValue != null) {
            if (levelValue < 0) {
                throw new BuildException(path + ".level: must be 0 or more");
            }
            state.xp = GodLevels.xpForLevel(levelValue);
        } else {
            state.xp = xp == null ? 0L : xp;
        }
        Integer sacrifices = BuildJson.optInt(json, "sacrifices", path);
        state.sacrifices = sacrifices != null ? sacrifices : GodLevels.sacrificesForLevel(GodLevels.levelForXp(state.xp));
        Integer bonus = BuildJson.optInt(json, "bonus_points", path);
        state.bonusPoints = bonus == null ? 0 : bonus;
        Integer altar = BuildJson.optInt(json, "altar_completions", path);
        state.altarCompletions = altar == null ? 0 : altar;

        List<String> nodeIds = new ArrayList<>();
        JsonArray nodes = BuildJson.optArray(json, "nodes", path);
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                String id = BuildJson.asString(nodes.get(i), path + ".nodes[" + i + "]");
                GodNode node = tree.getNode(id);
                if (node == null) {
                    throw new BuildException(path + ".nodes[" + i + "]: no node '" + id + "' in the " + god.getName() + " tree");
                }
                if (!node.enabled()) {
                    report.bypassed(path + ": node " + id + " is disabled in the game's tree");
                }
                nodeIds.add(id);
            }
        }
        for (String id : connectedOrder(tree, nodeIds, path, report)) {
            GodNode node = tree.getNode(id);
            state.treeNodes.add(id);
            state.spentPoints.merge(node.ledgerKey(), node.cost(), Integer::sum);
        }
        int level = GodLevels.gatedLevel(state.xp, state.sacrifices);
        int reputationPoints = GodLevels.reputationPoints(reputation);
        int total = state.totalPoints(level, reputationPoints);
        int spent = state.spentPoints();
        if (spent > total) {
            int missing = spent - total;
            state.bonusPoints += missing;
            report.bypassed(path + ": the nodes cost " + spent + " points but level " + level + " and " + reputation
                    + " reputation give " + total + "; granted " + missing + " bonus points to cover them");
        }
        if (!state.treeNodes.isEmpty() && reputation < GodLevels.chartingReputation()) {
            report.bypassed(path + ": " + reputation + " reputation is short of the " + GodLevels.chartingReputation()
                    + " needed to chart the constellation");
        }
        JsonArray transfers = BuildJson.optArray(json, "transfers", path);
        if (transfers != null) {
            int slots = GodLevels.minorTransferSlots(level);
            for (int i = 0; i < transfers.size(); i++) {
                String effect = BuildJson.asString(transfers.get(i), path + ".transfers[" + i + "]");
                state.minorTransfers.add(effect);
                if (effect.isEmpty()) {
                    continue;
                }
                if (i >= slots) {
                    report.bypassed(path + ".transfers[" + i + "]: level " + level + " opens only " + slots + " transfer slots");
                }
                if (!MinorTransferSlots.isTransferable(god, effect, state.spentPoints)) {
                    report.bypassed(path + ".transfers[" + i + "]: " + effect + " is not a learned minor star of "
                            + god.getName() + "; the game drops it on the next slot write");
                }
            }
        }
        report.summary(god.getName() + ": level " + level + ", " + state.treeNodes.size() + " nodes, " + spent + " of "
                + state.totalPoints(level, reputationPoints) + " points spent");
        return state;
    }

    /** The nodes in an order where each touches an earlier one or is a root; disconnected nodes are reported. */
    private static List<String> connectedOrder(GodTreeModel tree, List<String> nodeIds, String path, BuildReport report) {
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
                report.bypassed(path + ": node " + id + " does not connect to a root through the build's other nodes");
                order.add(id);
            }
        }
        return order;
    }

    /** The target state of one build, ready to apply. Null fields are sections the build leaves alone. */
    public static final class Plan {
        int level;
        int exp;
        @Nullable CompoundTag stats;
        @Nullable Integer greedTier;
        int greedReputation;
        @Nullable Map<VaultGod, Integer> reputation;
        @Nullable AbilityTree abilities;
        @Nullable String selectedAbility;
        @Nullable TalentTree talents;
        @Nullable ExpertiseTree expertises;
        @Nullable PrestigeTree prestige;
        @Nullable List<String> activePrestige;
        @Nullable Map<VaultGod, GodAlignmentData.GodState> gods;

        /** The vault level the player has once this plan is applied. */
        public int targetLevel() {
            return this.level;
        }

        /** Applies the plan in dependency order: greed, level and points, reputation, trees, then gods. */
        public void apply(ServerPlayer player, BuildReport report) {
            MinecraftServer server = player.getServer();
            ServerLevel level = player.getLevel();
            if (this.greedTier != null) {
                PlayerGreedTreeData greed = PlayerGreedTreeData.get(server);
                if (greed.getGreedTier(player) != this.greedTier) {
                    greed.setGreedTier(player, this.greedTier);
                    report.note("Greed tier set to " + this.greedTier + "; this clears greed quest and challenge slots and "
                            + "rerolls the greed trader");
                }
                greed.setGreedReputation(player, this.greedReputation);
            }
            if (this.stats != null) {
                LevelSyncData.get(server).leaveTeam(player);
                PlayerVaultStatsData statsData = PlayerVaultStatsData.get(level);
                PlayerVaultStats stats = statsData.getVaultStats(player);
                stats.deserializeNBT(this.stats);
                stats.sync(server);
                statsData.setDirty();
                player.refreshTabListName();
            }
            if (this.reputation != null) {
                for (Map.Entry<VaultGod, Integer> entry : this.reputation.entrySet()) {
                    int have = PlayerReputationData.getReputation(player.getUUID(), entry.getKey());
                    int want = entry.getValue();
                    if (have == want) {
                        continue;
                    }
                    if (have >= 50) {
                        report.bypassed(entry.getKey().getName() + " reputation stays at " + have
                                + ": the game cannot lower reputation that reached 50 (wanted " + want + ")");
                        continue;
                    }
                    PlayerReputationData.addReputation(player.getUUID(), entry.getKey(), want - have);
                    int now = PlayerReputationData.getReputation(player.getUUID(), entry.getKey());
                    if (now != want) {
                        report.bypassed(entry.getKey().getName() + " reputation ended at " + now + " instead of " + want);
                    }
                }
            }
            if (this.abilities != null) {
                PlayerAbilitiesData.get(level).setAbilities(player, this.abilities);
                SkillContext context = SkillContext.of(player);
                if (this.selectedAbility != null) {
                    this.abilities.onQuickSelect(this.selectedAbility, context);
                }
                this.abilities.sync(context);
            }
            if (this.talents != null) {
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
            if (this.prestige != null) {
                PlayerPrestigePowersData data = PlayerPrestigePowersData.get(level);
                data.setPrestige(player, this.prestige);
                SkillContext context = SkillContext.ofPrestige(player);
                for (CraftingRecipePower power : this.prestige.getAll(CraftingRecipePower.class, Skill::isUnlocked)) {
                    power.learn(context);
                }
                if (this.activePrestige != null) {
                    SkillContext toggleContext = SkillContext.of(player);
                    for (String id : this.activePrestige) {
                        Skill skill = this.prestige.getForId(id).orElse(null);
                        if (skill instanceof TieredSkill tiered && tiered.getChild() instanceof ActivatePrestigePower power
                                && !power.isActive()) {
                            if (power.onEnable(toggleContext)) {
                                power.setActive(true);
                            } else {
                                report.bypassed("prestige.active: " + id + " refused to switch on");
                            }
                        }
                    }
                }
                this.prestige.sync(context);
                data.setDirty();
            }
            if (this.gods != null) {
                GodAlignmentData godData = GodAlignmentData.get(server);
                for (Map.Entry<VaultGod, GodAlignmentData.GodState> entry : this.gods.entrySet()) {
                    godData.putState(player, entry.getKey(), entry.getValue());
                }
                GodNodeCache.refresh(player);
                GodNodeTicker.reconcile(player);
            }
            if (this.abilities != null) {
                this.abilities.iterate(TieredSkill.class, tiered -> {
                    if (TenosAbilityCaps.isGated(tiered.getId())) {
                        int cap = TenosAbilityCaps.cap(tiered.getId(), player);
                        if (tiered.getUnmodifiedTier() > cap) {
                            report.bypassed("abilities.learned." + tiered.getId() + ": tier " + tiered.getUnmodifiedTier()
                                    + " is above the Tenos cap of " + cap + " for this player's god nodes");
                        }
                    }
                });
            }
        }
    }
}

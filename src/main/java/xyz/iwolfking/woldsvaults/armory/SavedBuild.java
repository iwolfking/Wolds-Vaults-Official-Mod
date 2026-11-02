package xyz.iwolfking.woldsvaults.armory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.GroupedSkill;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.skill.tree.SkillTree;
import iskallia.vault.skill.tree.TalentTree;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.builds.state.PlayerStateCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The tree half of a saved page: the {@link PlayerStateCodec#capture} JSON of the player at Save, plus the H-screen
 * stat snapshot the client sent with the Save. Read-only after creation.
 */
public final class SavedBuild {
    private static final int FORCE_LEVEL = 100_000;
    private static final int FORCE_POINTS = 100_000_000;

    private final JsonObject state;
    private final CompoundTag stats;
    private final String saver;
    private final long time;

    public SavedBuild(JsonObject state, CompoundTag stats, String saver, long time) {
        this.state = state;
        this.stats = stats;
        this.saver = saver;
        this.time = time;
    }

    /** Captures {@code player}'s trees and points now, with the client's stat snapshot. */
    public static SavedBuild capture(ServerPlayer player, CompoundTag stats) {
        JsonObject state = new JsonObject();
        PlayerStateCodec.capture(player, state);
        return new SavedBuild(state, stats, player.getGameProfile().getName(), System.currentTimeMillis());
    }

    public JsonObject state() {
        return this.state;
    }

    public CompoundTag stats() {
        return this.stats;
    }

    public String saver() {
        return this.saver;
    }

    public long time() {
        return this.time;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("state", this.state.toString());
        tag.put("stats", this.stats);
        tag.putString("saver", this.saver);
        tag.putLong("time", this.time);
        return tag;
    }

    @Nullable
    public static SavedBuild load(CompoundTag tag) {
        try {
            JsonObject state = JsonParser.parseString(tag.getString("state")).getAsJsonObject();
            return new SavedBuild(state, tag.getCompound("stats"), tag.getString("saver"), tag.getLong("time"));
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Armory: a saved build could not be read and is dropped ({})", e.toString());
            return null;
        }
    }

    public int level() {
        return intAt("player", "level");
    }

    public int points(String pool) {
        JsonObject player = obj(this.state, "player");
        return player == null ? 0 : intAt(obj(player, "points"), pool);
    }

    /** Saved learned tiers of one section: "abilities", "talents", "expertises" or "prestige". */
    public Map<String, Integer> learned(String section) {
        JsonObject json = obj(this.state, section);
        if (json != null && ("abilities".equals(section) || "prestige".equals(section))) {
            json = obj(json, "learned");
        }
        Map<String, Integer> out = new LinkedHashMap<>();
        if (json != null) {
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                out.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
        return out;
    }

    @Nullable
    public String selectedAbility() {
        JsonObject abilities = obj(this.state, "abilities");
        return abilities != null && abilities.has("selected") ? abilities.get("selected").getAsString() : null;
    }

    /** Saved god node ids for one god, in save order. */
    public List<String> godNodes(VaultGod god) {
        return godList(god, "nodes");
    }

    public List<String> godTransfers(VaultGod god) {
        return godList(god, "transfers");
    }

    public long godXp(VaultGod god) {
        JsonObject gods = obj(this.state, "gods");
        JsonObject json = gods == null ? null : obj(gods, PlayerStateCodec.godKey(god));
        return json != null && json.has("xp") ? json.get("xp").getAsLong() : 0L;
    }

    public int godSacrifices(VaultGod god) {
        JsonObject gods = obj(this.state, "gods");
        JsonObject json = gods == null ? null : obj(gods, PlayerStateCodec.godKey(god));
        return json != null && json.has("sacrifices") ? json.get("sacrifices").getAsInt() : 0;
    }

    public int reputation(VaultGod god) {
        JsonObject player = obj(this.state, "player");
        return player == null ? 0 : intAt(obj(player, "reputation"), PlayerStateCodec.godKey(god));
    }

    private List<String> godList(VaultGod god, String key) {
        List<String> out = new ArrayList<>();
        JsonObject gods = obj(this.state, "gods");
        JsonObject json = gods == null ? null : obj(gods, PlayerStateCodec.godKey(god));
        if (json != null && json.has(key)) {
            json.getAsJsonArray(key).forEach(element -> out.add(element.getAsString()));
        }
        return out;
    }

    /** The saved ability tree learned on a fresh config copy, for display. Unknown ids are logged and skipped. */
    @Nullable
    public AbilityTree displayAbilities() {
        AbilityTree tree = ModConfigs.ABILITIES.get().map(config -> (AbilityTree) config.copy()).orElse(null);
        if (tree == null) {
            return null;
        }
        SkillContext force = force();
        for (Map.Entry<String, Integer> entry : learned("abilities").entrySet()) {
            Skill skill = tree.getForId(entry.getKey()).orElse(null);
            if (skill instanceof TieredSkill tiered && tiered.getParent() instanceof SpecializedSkill specialized) {
                specialized.specialize(entry.getKey(), force);
                for (int i = 0; i < entry.getValue() && tiered.getUnmodifiedTier() < tiered.getTiers().size(); i++) {
                    tiered.learn(force, true);
                }
            } else {
                WoldsVaults.LOGGER.warn("Armory: saved ability {} is not an ability specialization in this game; shown unlearned",
                        entry.getKey());
            }
        }
        return tree;
    }

    @Nullable
    public TalentTree displayTalents() {
        TalentTree tree = ModConfigs.TALENTS.get().map(config -> (TalentTree) config.copy()).orElse(null);
        return tree == null ? null : learnForced(tree, learned("talents"), "talent");
    }

    public ExpertiseTree displayExpertises() {
        return learnForced(ModConfigs.EXPERTISES.getAll().copy(), learned("expertises"), "expertise");
    }

    @Nullable
    public PrestigeTree displayPrestige() {
        PrestigeTree tree = ModConfigs.PRESTIGE_POWERS.get().map(config -> (PrestigeTree) config.copy()).orElse(null);
        return tree == null ? null : learnForced(tree, learned("prestige"), "prestige power");
    }

    /**
     * Learns {@code tiers} on {@code tree} with no gates. Used only to rebuild a saved tree for display; the
     * armory's real Load path is {@code TreePlanner}, which runs the game's gates.
     */
    public static <T extends SkillTree> T learnForced(T tree, Map<String, Integer> tiers, String what) {
        SkillContext force = force();
        for (Map.Entry<String, Integer> entry : tiers.entrySet()) {
            Skill skill = tree.getForId(entry.getKey()).orElse(null);
            if (skill == null || skill.getParent() instanceof TieredSkill) {
                WoldsVaults.LOGGER.warn("Armory: saved {} {} does not exist in this game; shown unlearned", what, entry.getKey());
                continue;
            }
            if (skill.getParent() instanceof GroupedSkill grouped) {
                grouped.select(entry.getKey());
            }
            if (skill instanceof TieredSkill tiered) {
                for (int i = 0; i < entry.getValue() && tiered.getUnmodifiedTier() < tiered.getTiers().size(); i++) {
                    tiered.learn(force, true);
                }
            } else if (skill instanceof LearnableSkill learnable && !(skill instanceof GroupedSkill)
                    && !(skill instanceof SpecializedSkill)) {
                learnable.learn(force);
            }
        }
        return tree;
    }

    private static SkillContext force() {
        return new SkillContext(FORCE_LEVEL, FORCE_POINTS, FORCE_POINTS, SkillSource.empty());
    }

    private int intAt(String objectKey, String key) {
        return intAt(obj(this.state, objectKey), key);
    }

    private static int intAt(@Nullable JsonObject json, String key) {
        return json != null && json.has(key) ? json.get(key).getAsInt() : 0;
    }

    @Nullable
    private static JsonObject obj(@Nullable JsonObject json, String key) {
        return json != null && json.has(key) && json.get(key).isJsonObject() ? json.getAsJsonObject(key) : null;
    }
}

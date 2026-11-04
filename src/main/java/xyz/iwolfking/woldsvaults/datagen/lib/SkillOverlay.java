package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;

import java.util.Optional;

/** Builds skills for VHAPI skill-tree overlays through the base mod's skill adapter. */
public final class SkillOverlay {
    private SkillOverlay() {
    }

    /** A tiered skill read through the base mod's skill adapter, so each tier is the registered type's own object. */
    public static TieredSkill tiered(String id, String name, Tier... tiers) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tiered");
        json.addProperty("id", id);
        json.addProperty("name", name);
        JsonArray array = new JsonArray();
        for (Tier tier : tiers) {
            array.add(tier.json);
        }
        json.add("tiers", array);
        Optional<Skill> skill = Adapters.SKILL.readJson(json);
        if (skill.isEmpty() || !(skill.get() instanceof TieredSkill tiered)) {
            throw new IllegalStateException("Skill " + id + " did not read as a tiered skill");
        }
        return tiered;
    }

    /** One tier of a tiered skill, written as the JSON its registered type reads. */
    public static final class Tier {
        private final JsonObject json = new JsonObject();

        private Tier(String type) {
            this.json.addProperty("type", type);
        }

        public static Tier of(String type) {
            return new Tier(type);
        }

        public Tier with(String key, Number value) {
            this.json.addProperty(key, value);
            return this;
        }

        public Tier with(String key, String value) {
            this.json.addProperty(key, value);
            return this;
        }

        public Tier costs(int learnPointCost, int learnKnowledgeCost, int regretPointCost, int unlockLevel) {
            this.json.addProperty("learnPointCost", learnPointCost);
            this.json.addProperty("learnKnowledgeCost", learnKnowledgeCost);
            this.json.addProperty("regretPointCost", regretPointCost);
            this.json.addProperty("unlockLevel", unlockLevel);
            return this;
        }

        public Tier greedTier(int tier, int requiredGreedTier) {
            this.json.addProperty("tier", tier);
            this.json.addProperty("requiredGreedTier", requiredGreedTier);
            return this;
        }

        public Tier id(String id) {
            this.json.addProperty("id", id);
            return this;
        }
    }
}

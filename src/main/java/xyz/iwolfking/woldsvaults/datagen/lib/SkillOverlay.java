package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import xyz.iwolfking.vhapi.mixin.accessors.TieredSkillAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds skills for VHAPI skill-tree overlays and applies those overlays to a base tree the same way VHAPI's
 * {@code TalentConfigLoader} and {@code PrestigePowersConfigLoader} do, so datagen text can read the merged tree.
 */
public final class SkillOverlay {
    private SkillOverlay() {
    }

    /**
     * Swaps the tiers of every top-level tiered skill in {@code base} whose id an overlay skill shares, as VHAPI's
     * "replace" files do; talents also take the overlay's max learnable tier, prestige powers keep their own.
     */
    public static void replaceTiers(List<Skill> base, List<? extends Skill> overlay, boolean withMaxLearnableTier) {
        List<Skill> targets = new ArrayList<>();
        for (Skill replacement : overlay) {
            first(base, replacement.getId()).ifPresent(targets::add);
        }
        for (Skill skill : base) {
            if (targets.contains(skill) && skill instanceof TieredSkill tiered) {
                TieredSkill source = (TieredSkill) first(overlay, skill.getId()).orElseThrow();
                ((TieredSkillAccessor) tiered).setTiers(source.getTiers());
                if (withMaxLearnableTier) {
                    ((TieredSkillAccessor) tiered).setMaxLearnableTier(source.getMaxLearnableTier());
                }
            }
        }
    }

    private static Optional<Skill> first(List<? extends Skill> skills, String id) {
        for (Skill skill : skills) {
            if (skill.getId().equals(id)) {
                return Optional.of(skill);
            }
        }
        return Optional.empty();
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

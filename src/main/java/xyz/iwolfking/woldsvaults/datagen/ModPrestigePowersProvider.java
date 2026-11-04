package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.PrestigePowersConfig;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.PrestigeTree;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.SkillOverlay;
import xyz.iwolfking.woldsvaults.datagen.lib.SkillOverlay.Tier;

import java.util.ArrayList;
import java.util.List;

/**
 * The greed rework's overlay on the base mod's {@code prestige_powers.json}. VHAPI's prestige loader appends the
 * powers of the additions file and swaps the tiers of each power in the "replace" file into the base power of the
 * same id.
 */
public class ModPrestigePowersProvider extends AbstractPrestigePowerProvider {
    public static final String ADDITIONS_FILE = "greed_powers";
    public static final String REPLACE_FILE = "greed_powers_replace";

    protected ModPrestigePowersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addConfig(ADDITIONS_FILE, config(additions()));
        addConfig(REPLACE_FILE, config(replacements()));
    }

    /** Base prestige powers whose tiers the greed rework replaces. */
    public static List<Skill> replacements() {
        return List.of(
                power("Toolsmith", "Echoing Hammersmith", tier("tool_capacity_power").with("capacityIncrease", 50).costs(0, 100, 1, 0).greedTier(2, 2)),
                power("BlackMarketRerolls", "Whispers of the Market", tier("black_market_rerolls_power").with("costMultiplier", 3).costs(0, 400, 1, 0).greedTier(4, 4).id("black_market_rerolls_1")),
                power("CrateTier", "Bountiful Crates", tier("crate_tier_power").with("extraTier", 1).costs(0, 600, 1, 0).greedTier(5, 5).id("crate_tier_1")),
                power("SuperCrystals", "Crystalweaver's Gift", tier("super_crystals_power").with("extraCapacity", 75).costs(0, 800, 1, 0).greedTier(6, 6).id("super_crystals_1")),
                power("NoTrinketUses", "Immortal Trinkets", tier("no_uses_power").with("itemId", "the_vault:trinket").costs(0, 1200, 1, 0).greedTier(8, 8).id("trinket_uses_1")),
                power("StepUpPower", "Stride of the Frog", tier("activatable_trinket_power").with("trinketEffectId", "the_vault:high_heels").costs(0, 200, 1, 0).greedTier(3, 3).id("step_up_1")),
                power("BlackMarketer", "The Black Marketer", tier("black_marketer_power").costs(0, 1600, 1, 0).greedTier(10, 10).id("black_marketer_1")),
                power("PrismaticPouch", "Prism Possessor", tier("crafting_recipe_power").with("recipeUnlock", "woldsvaults:prismatic_trinket_pouch").costs(0, 2400, 1, 0).greedTier(13, 13).id("prism_possessor_1")),
                power("Fruity", "Bloom of Time", tier("gear_attribute_power").with("attribute", "the_vault:fruit_effectiveness").with("value", 0.5F).costs(0, 2000, 1, 0).greedTier(12, 12).id("fruity_1")),
                power("MasterfulPower", "Aspect of Mastery", tier("masterful_power").costs(0, 2400, 1, 0).greedTier(13, 13).id("masterful_power_1")),
                power("ShieldOfLastingGuard", "Shield of Lasting Guard", tier("gear_attribute_power").with("attribute", "the_vault:block_cap").with("value", 0.1F).costs(0, 2800, 1, 0).greedTier(14, 14).id("shield_of_lasting_guard_1")),
                power("BarrierOfResilience", "Barrier of Resilience", tier("gear_attribute_power").with("attribute", "the_vault:resistance_cap").with("value", 0.05F).costs(0, 2400, 1, 0).greedTier(13, 13).id("barrier_of_resilience_1")),
                power("WeaverOfTime", "Weaver of Time", tier("gear_attribute_power").with("attribute", "the_vault:cooldown_reduction_cap").with("value", 0.05F).costs(0, 2800, 1, 0).greedTier(14, 14).id("weaver_of_time_1")),
                power("HealthIncrease", "Blessing of the Dryad", tier("gear_attribute_power").with("attribute", "the_vault:health").with("value", 30.0F).costs(0, 1800, 1, 0).greedTier(11, 11).id("health_increase_1"))
        );
    }

    /** Prestige powers the greed rework adds, in tree order. */
    public static List<Skill> additions() {
        return List.of(
                power("ToolsmithII", "Echoing Toolsmith II", tier("tool_capacity_power").with("capacityIncrease", 75).costs(0, 1200, 1, 0).greedTier(8, 8).id("toolsmith_2")),
                power("SuperCrystalsII", "Crystalweaver's Gift II", tier("super_crystals_power").with("extraCapacity", 25).costs(0, 1400, 1, 0).greedTier(9, 9).id("super_crystals_2")),
                power("SuperCrystalsIII", "Crystalweaver's Gift III", tier("super_crystals_power").with("extraCapacity", 25).costs(0, 3200, 1, 0).greedTier(15, 15).id("super_crystals_3")),
                power("SpiritsHandII", "Spirit's Hand II", tier("reach_cap_power").with("reachIncrease", 1.0F).costs(0, 2800, 1, 0).greedTier(14, 14).id("spirits_hand_2")),
                power("FruityII", "Bloom of Time II", tier("gear_attribute_power").with("attribute", "the_vault:fruit_effectiveness").with("value", 0.5F).costs(0, 3200, 1, 0).greedTier(15, 15).id("fruity_2")),
                power("GodsDisciple", "God's Disciple", tier("god_experience_power").with("experienceIncrease", 0.2F).with("altarBaseMultiplier", 2.0F).costs(0, 800, 1, 0).greedTier(6, 6).id("gods_disciple_1")),
                power("GodsDiscipleII", "God's Disciple II", tier("god_experience_power").with("experienceIncrease", 0.15F).costs(0, 2000, 1, 0).greedTier(12, 12).id("gods_disciple_2")),
                power("RelicHunter", "Relic Hunter", tier("relic_hunter_power").with("chanceMultiplier", 0.5F).costs(0, 2000, 1, 0).greedTier(12, 12).id("relic_hunter_1")),
                power("RelicHunterII", "Relic Hunter II", tier("relic_hunter_power").with("chanceMultiplier", 0.5F).costs(0, 3200, 1, 0).greedTier(15, 15).id("relic_hunter_2")),
                power("LegendOfTime", "Legend of Time", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_time")),
                power("LegendOfPower", "Legend of Power", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_power")),
                power("LegendOfRiches", "Legend of Riches", tier("unreleased_power").costs(0, 25000, 1, 0).greedTier(16, 16).id("legend_of_riches"))
        );
    }

    /** The prestige config as VHAPI leaves it after applying this provider's files to {@code base}. */
    public static PrestigePowersConfig withOverlay(PrestigePowersConfig base) {
        SkillOverlay.replaceTiers(base.tree.skills, replacements(), false);
        base.tree.skills.addAll(additions());
        return base;
    }

    private static PrestigePowersConfig config(List<Skill> skills) {
        PrestigePowersConfig config = new PrestigePowersConfig();
        config.tree = new PrestigeTree();
        config.tree.skills = new ArrayList<>(skills);
        return config;
    }

    private static Skill power(String id, String name, Tier... tiers) {
        return SkillOverlay.tiered(id, name, tiers);
    }

    private static Tier tier(String type) {
        return Tier.of(type);
    }
}
